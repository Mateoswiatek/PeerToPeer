package pl.agh.task;

import lombok.AllArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.middleware.task.SHA256TaskExecutionStrategy;
import pl.agh.task.factory.TaskFactory;
import pl.agh.task.mapper.BatchMapper;
import pl.agh.task.mapper.TaskMapper;
import pl.agh.task.model.Batch;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.*;
import pl.agh.task.model.enumerated.BatchStatus;
import pl.agh.task.model.enumerated.TaskStatus;
import pl.agh.task.observer.TaskStatusLogger;
import pl.agh.task.ports.inbound.TaskController;
import pl.agh.task.ports.outbound.*;

import java.math.BigInteger;
import java.util.*;

@AllArgsConstructor
public class TaskControllerImpl implements TaskController {
    private final BatchRepositoryPort batchRepository;
    private final TaskRepositoryPort taskRepositoryPort;
    private final TaskMessageSenderPort taskMessagePort;
    private final TaskFactory taskFactory;
    private final DoneTaskProcessor doneTaskProcessor;
    private final Logger logger = Logger.getInstance();

    private boolean w8ForMemoryDump;
    private final Random random = new Random();
    private final Map<UUID, TaskThread> taskThreads = new HashMap<>();

    /**
     * Zawsze przychodzi nowy task od Klienta.
     * @param newTaskRequest request from Client / external system
     * @return taskId
     */
    @Override
    public UUID createNewTask(NewTaskDto newTaskRequest) {
        Task newTask = prepareAndStartNewTaskFromRequest(newTaskRequest);
        this.startTask(newTask.getTaskId());

        taskMessagePort.sendTaskUpdateMessage(TaskMapper.toTaskUpdateMessageDto(newTask));
        return newTask.getTaskId();
    }

    /**
     * Update tasków, zarówno nowe jak i stare historyczne
     * @param taskUpdateMessageDto
     */
    @Override
    public void updateTask(TaskUpdateMessageDto taskUpdateMessageDto) {
        logger.info("updateTask: " + taskUpdateMessageDto.getTaskId());

        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();
        UUID taskId = taskUpdateMessageDto.getTaskId();
        Optional<Task> optTask = taskRepositoryPort.getById(taskId);

        // Mamy go w bazie, Jest oznaczony jako zrobiony.
        if(optTask.isPresent() && optTask.get().getTaskStatus().equals(TaskStatus.DONE)) {
            logger.info("TaskController.updateTask - skip");
            return;
        }

        // Przyszedł nowy, jako zrobiony
        if(taskUpdateMessageDto.getTaskStatus().equals(TaskStatus.DONE)){
            logger.info("TaskController.updateTask - received done task");

            Task task = TaskMapper.toTask(taskUpdateMessageDto, strategy);
            taskRepositoryPort.save(task);
            this.processDoneTask(task);
            return;
        }

        // Liczymy tego taska
        if(taskThreads.containsKey(taskId)) {
            logger.info("TaskController.updateTask - received task that already computing");

            // Jeśli jest result dla tego taska
            // Zatrzymujemy, zapisujemy jako zrobionego oraz procesujemy jako wykonanego
            if(taskUpdateMessageDto.getResult() != null && !taskUpdateMessageDto.getResult().isEmpty()) {
                logger.info("TaskController.updateTask - received done task that already computing. Process doneTask");

                taskThreads.get(taskId).stopTask();
                Task task = taskRepositoryPort.save(TaskMapper.toTask(taskUpdateMessageDto, strategy));
                this.processDoneTask(task);
            }
        } else {
            logger.info("TaskController.updateTask - received new Task from network, start processing without send message");
            // Nie mamy go w bazie, nie przyszedł jako zrobiony, nie liczymy go == nowy task
            // Nie wysyłamy powiadomienia, bo dostaliśmy go z sieci.
            Task task = this.prepareAndStartNewTaskFromNetwork(taskUpdateMessageDto);
            this.startTask(task.getTaskId());
        }
    }

    private Task prepareAndStartNewTaskFromNetwork(TaskUpdateMessageDto newTaskRequest) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();
        Task task = taskFactory.createTaskFromNetwork(newTaskRequest, strategy);
        return prepareNewTask(task);
    }

    private Task prepareAndStartNewTaskFromRequest(NewTaskDto newTaskRequest) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();
        Task task = taskFactory.createNewTaskFromRequest(newTaskRequest, strategy);
        return prepareNewTask(task);
    }

    private Task prepareNewTask(Task task) {
        task.addObserver(new TaskStatusLogger());
        task.setTaskStatus(TaskStatus.IN_PROGRESS);

        Task saved = taskRepositoryPort.save(task);
        logger.info("Saved prepared task: " + saved.getTaskId());
        this.initializeBatches(saved);
        return saved;
    }

    @Override
    public Optional<TaskUpdateMessageRequestDto> updateBatch(BatchUpdateDto batchUpdateMessage) {
        UUID taskId = batchUpdateMessage.getTaskId();
        Long batchId = batchUpdateMessage.getBatchId();
        boolean computeThisTask = taskThreads.containsKey(taskId);

        if(computeThisTask) {
            TaskThread taskThread = taskThreads.get(taskId);
            // Task is completed
            if (batchUpdateMessage.getBatchStatus().equals(BatchStatus.FOUND)) {
                taskRepositoryPort.getById(taskId).ifPresent(task -> {
                    logger.info("TaskController.updateBatch - received FOUND batch update, stop the task");
                    task.complete(batchUpdateMessage.getResult());
                    taskRepositoryPort.save(task);
                    this.processDoneTask(task);
                });
            } else if(batchUpdateMessage.getBatchStatus().equals(BatchStatus.DONE) && taskId.equals(taskThread.getTaskId()) && batchId.equals(taskThread.getCurrentBatch().getBatchId())) {
                // Update przyszedł z tym batchem, który teraz robiliśmy.
                logger.info("TaskController.updateBatch - received DONE the same batch as we, start new batch.");
                taskThread.stopTask();
                this.startTask(taskId);
            } else { // Zwykły update innego batcha. Ktoś zrobił.
                logger.info("TaskController.updateBatch - other update from the same task");
                batchRepository.updateStatus(batchUpdateMessage.getTaskId(), batchUpdateMessage.getBatchId(), batchUpdateMessage.getBatchStatus());
            }
        } else if(taskRepositoryPort.getById(batchUpdateMessage.getTaskId()).isEmpty() && !w8ForMemoryDump) {
            w8ForMemoryDump = true;
            logger.info("TaskController.updateBatch - we didnt know about this task. Start getting more info. taskId: " + batchUpdateMessage.getTaskId());
            // Task o którym nie wiedzieliśmy (podłączyliśmy się po jego rozpoczęciu lub inne braki).
            return Optional.of(TaskUpdateMessageRequestDto.create(batchUpdateMessage.getTaskId()));
        }

        return Optional.empty();
    }

    @Override
    public MemoryDumpDto getMemoryDumpMessage() {
        taskRepositoryPort.findByStatus(TaskStatus.DONE).forEach(this::processDoneTask);

        List<TaskUpdateMessageDto> tasks = taskRepositoryPort.findAll().stream().map(TaskMapper::toTaskUpdateMessageDto).toList();
        List<BatchUpdateDto> batches = batchRepository.findAll().stream().filter(b -> !b.getStatus().equals(BatchStatus.DONE)).map(BatchMapper::toBatchUpdateDto).toList();
        MemoryDumpDto memoryDumpDto = new MemoryDumpDto(tasks, batches);
        logger.info("return memoryDumpDto, batchUpdateDtos.size=" + memoryDumpDto.getBatchUpdateDtos().size());
        logger.debug("memoryDumpDto" + memoryDumpDto);
        return memoryDumpDto;
    }

    //TODO (14.01.2025): Dodać jakiegoś blocka, tak aby najpierw się robił memory dump ? bo przy updateach ciągle leci wysyłanie requesta :'(
    @Override
    public MemoryDumpDto getMemoryDumpMessage(UUID taskId) {
        taskRepositoryPort.getById(taskId).ifPresent(x -> {
            if(x.getTaskStatus().equals(TaskStatus.DONE)){
                this.processDoneTask(x);
            }
        });

        List<TaskUpdateMessageDto> tasks = taskRepositoryPort.getById(taskId).stream().map(TaskMapper::toTaskUpdateMessageDto).toList();
        List<BatchUpdateDto> batches = batchRepository.findAllByTaskId(taskId).stream().filter(b -> !b.getStatus().equals(BatchStatus.DONE)).map(BatchMapper::toBatchUpdateDto).toList();
        return new MemoryDumpDto(tasks, batches);
    }

    @Override
    public void updateTasks(MemoryDumpDto memoryDumpDto) {
        logger.info("updateTasks - invoked");
        memoryDumpDto.getTaskUpdateMessageDtos().forEach(this::updateTask);

        // Dostajemy batche, z aktualnie robionych tasków. Możemy przefiltrować po nie zrobionych i tylko je zapisać?
        batchRepository.updateStatusFromDump(memoryDumpDto.getBatchUpdateDtos());

        logger.info("updateTasks - done: " + memoryDumpDto);

        taskRepositoryPort.findAll().stream().filter(task -> !task.getTaskStatus().equals(TaskStatus.DONE)).findFirst().map(Task::getTaskId).ifPresent(uuid -> {
            logger.info("uuid: " + uuid);
            this.startTask(uuid);
        });
        w8ForMemoryDump = false;
    }

    private void startTask(UUID taskId) {
        logger.info("Start new task: " + taskId);
        taskThreads.computeIfAbsent(
                taskId,
                newThreadTaskId -> {
                    TaskThread taskThread = new TaskThread(
                            this::getNextBatch,
                            this::callbackBatchUpdate,
                            id -> taskRepositoryPort.getById(id).orElseThrow(() -> new RuntimeException("Task not found: " + id)), // Funkcja dostarczająca Task
                            taskId, // ID zadania
                            null // aktualny batch
                    );

                    Thread thread = new Thread(taskThread);
                    thread.start();
                    return taskThread;
                });
    }

    private void processDoneTask(Task task) {
        doneTaskProcessor.processDoneTask(task);

        logger.info(String.valueOf(batchRepository.findAllByTaskId(task.getTaskId()).size()));
        batchRepository.deleteByTaskId(task.getTaskId());
        logger.info(String.valueOf(batchRepository.findAllByTaskId(task.getTaskId()).size()));
    }

    private void stopTask(UUID taskId) {
        taskThreads.get(taskId).stopTask();
    }

    private void callbackBatchUpdate(BatchUpdateDto batchUpdateMessage) {
        logger.info("Batch update for task: " + batchUpdateMessage.getTaskId() + " batch: " + batchUpdateMessage.getBatchId() + " status: " + batchUpdateMessage.getBatchStatus());
        batchRepository.updateStatus(batchUpdateMessage.getTaskId(), batchUpdateMessage.getBatchId(), batchUpdateMessage.getBatchStatus());
        taskMessagePort.sendBatchUpdateMessage(batchUpdateMessage);
        if(batchUpdateMessage.getBatchStatus().equals(BatchStatus.FOUND)) {

            Task task = taskRepositoryPort.getById(batchUpdateMessage.getTaskId()).orElseThrow(() -> new RuntimeException("Task not found"));
            task.complete(batchUpdateMessage.getResult());

            taskRepositoryPort.save(task);
            this.processDoneTask(task);

            taskMessagePort.sendTaskUpdateMessage(TaskMapper.toTaskUpdateMessageDto(task));
            logger.info("Stop task: " + batchUpdateMessage.getTaskId());
            this.stopTask(batchUpdateMessage.getTaskId());
        }
    }

    // Methods for batches and algorithm

    private void initializeBatches(Task task) {
        logger.info("Initialize Batches for task: " + task.getTaskId());
        BigInteger totalPermutations = calculateTotalPermutations(task.getAlphabet().length(), task.getMaxLength());

        List<Batch> batches = new ArrayList<>();
        BigInteger currentMin = BigInteger.ZERO;
        BigInteger maxBatchSize = BigInteger.valueOf(task.getMaxBatchSize());

        UUID taskId = task.getTaskId();
        long batchId = 1L;
        while (currentMin.compareTo(totalPermutations) < 0) {
            BigInteger currentMax = currentMin.add(maxBatchSize).subtract(BigInteger.ONE);
            if (currentMax.compareTo(totalPermutations) >= 0) {
                currentMax = totalPermutations.subtract(BigInteger.ONE);
            }

            Batch batch = new Batch(
                    taskId,
                    batchId++,
                    currentMin.toString(),
                    currentMax.toString(),
                    BatchStatus.NOT_DONE
            );

            batches.add(batch);
            currentMin = currentMax.add(BigInteger.ONE);
        }

        batchRepository.saveAll(batches);
        logger.info("All batches initialized for task: " + task.getTaskId() + " total of: " + batches.size());
    }

    private BigInteger calculateTotalPermutations(int alphabetLength, Long maxLength) {
        BigInteger total = BigInteger.ZERO;
        BigInteger currentPower = BigInteger.ONE;
        BigInteger alphabetSize = BigInteger.valueOf(alphabetLength);

        for (int i = 1; i <= maxLength; i++) {
            currentPower = currentPower.multiply(alphabetSize);
            total = total.add(currentPower);
        }

        return total;
    }

    private Optional<Batch> getNextBatch(UUID taskId) {
        logger.info("Get next batch");
        List<Batch> batches = batchRepository.findAllByStatusAndTaskId(BatchStatus.NOT_DONE, taskId);
        if (batches.isEmpty()) {
            logger.info("All batches BOOKED");
            batches = batchRepository.findAllByStatusAndTaskId(BatchStatus.BOOKED, taskId);
        }

        if (batches.isEmpty()) {
            logger.info("All batches DONE");
            return Optional.empty();
        }
        logger.info("Return random available batch");
        return Optional.of(batches.get(random.nextInt(batches.size())));
    }
}
