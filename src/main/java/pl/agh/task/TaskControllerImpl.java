package pl.agh.task;

import lombok.AllArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.middleware.task.DoneTaskProcessor;
import pl.agh.task.factory.TaskFactory;
import pl.agh.task.impl.SHA256TaskExecutionStrategy;
import pl.agh.task.impl.TaskExecutionStrategy;
import pl.agh.task.mapper.TaskMapper;
import pl.agh.task.model.Batch;
import pl.agh.task.model.dto.TaskUpdateMessageDto;
import pl.agh.task.model.enumerated.BatchStatus;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.Task;
import pl.agh.task.model.enumerated.TaskStatus;
import pl.agh.task.observer.TaskStatusLogger;
import pl.agh.task.ports.inbound.TaskController;
import pl.agh.task.ports.outbound.BatchRepositoryPort;
import pl.agh.task.ports.outbound.TaskMessageSenderPort;
import pl.agh.task.ports.outbound.TaskRepositoryPort;

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

    private final Random random = new Random();
    private final Map<UUID, TaskThread> taskThreads = new HashMap<>();

    /**
     * Zawsze przychodzi nowy task od Klienta.
     * @param newTaskRequest request from Client / external system
     * @return taskId
     */
    @Override
    public UUID createNewTask(NewTaskDto newTaskRequest) {
        Task newTask = prepareAndStartNewTask(newTaskRequest);

        taskMessagePort.sendTaskUpdateMessage(TaskMapper.toTaskUpdateMessageDto(newTask));
        return newTask.getTaskId();
    }

    /**
     * Update tasków, zarówno nowe jak i stare historyczne
     * @param taskUpdateMessageDto
     */
    @Override
    public void updateTask(TaskUpdateMessageDto taskUpdateMessageDto) {
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
            this.prepareAndStartNewTask(TaskMapper.toNewTaskDto(taskUpdateMessageDto));
        }
    }

    private Task prepareAndStartNewTask(NewTaskDto newTaskRequest) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();

        Task task = taskFactory.createTask(newTaskRequest, strategy);
        task.addObserver(new TaskStatusLogger());
        task.setTaskStatus(TaskStatus.IN_PROGRESS);

        Task saved = taskRepositoryPort.save(task);
        logger.info("Saved prepared task: " + saved.getTaskId());
        this.initializeBatches(saved);
        this.startTask(saved.getTaskId());

        return saved;
    }

    private void startTask(UUID taskId) {
        logger.info("Start new task: " + taskId);
        taskThreads.computeIfAbsent(
                taskId,
                newThreadTaskId -> {
                    TaskThread taskThread = new TaskThread(
                            this::getNextBatch,
                            this::callbackBatchUpdate,
                            id -> taskRepositoryPort.getById(id)
                                    .orElseThrow(() -> new RuntimeException("Task not found: " + id)), // Funkcja dostarczająca Task
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
        batchRepository.deleteByTaskId(task.getTaskId());
    }




////    To ma być na zwrotce do noda ktory się podłączył.
//    public MemoryDumpMessage getMemoryDump() {
//        List<TaskUpdateMessage> tasks = taskRepositoryPort.findAll().stream().map(task -> {
//            logger.info("Task ID: " + task.getTaskId());
//            return TaskMapper.toTaskFromNetworkMessage(task);
//        }).toList();
//        List<Batch> batches = batchRepository.findAll();
//
//        List<TaskUpdateMessage> doneTasks = tasks.stream().filter(t -> t.getTaskStatus().equals(TaskStatus.DONE)).toList();
//
//        List<BatchUpdateDto> batchesUpdateDtoList = batchRepository.findAll().stream()
//                .filter(b -> !b.getStatus().equals(BatchStatus.FOUND))
//                .map(b -> BatchMapper.toBatchUpdateDto(b, null)).collect(toList());
//
//        List<BatchUpdateDto> doneBatches = batches.stream().filter(b -> b.getStatus().equals(BatchStatus.FOUND)).map(b -> {
//                TaskUpdateMessage thisTask = doneTasks.stream().filter(dt -> dt.getTaskId().equals(b.getTaskId())).findFirst().orElseThrow();
//                return BatchMapper.toBatchUpdateDto(b, thisTask.getResult());
//            }).toList();
//
//        batchesUpdateDtoList.addAll(doneBatches);
//
//        return new MemoryDumpMessage("MemoryDumpMessage", tasks, batchesUpdateDtoList);
//    }
    /**
     *  Przetwarza wiadomości z innych systeów pracujących przy tym samym zadaniu.
     *  Aktualizuje swoją bazę wiedzy, Jeśli pracuje nad danym taskiem to:
     *  - Jeśli dostał aktualizację z tego samego batcha informującą, że jest zrobiony, to przerywa tego batcha i zaczyna innego.
     *  - Jeśli dostał info, że znaleziono rozwiązanie dla danego zadania, to przerywa.
     * @param batchUpdateMessage
     */
    public void receiveBatchUpdateMessage(BatchUpdateDto batchUpdateMessage) {
        logger.info("Receive batch update message");
        batchRepository.updateStatus(batchUpdateMessage.getTaskId(), batchUpdateMessage.getBatchId(), batchUpdateMessage.getBatchStatus());

        UUID taskId = batchUpdateMessage.getTaskId();
        TaskThread taskThread = taskThreads.get(taskId);

        if (taskThread != null) {
            if (batchUpdateMessage.getBatchStatus().equals(BatchStatus.DONE)) {
                taskRepositoryPort.getById(taskId).ifPresent(task -> {
                    if (batchRepository.findAllByStatusAndTaskId(BatchStatus.NOT_DONE, taskId).isEmpty()) {
                        task.complete(batchUpdateMessage.getResult());
                        taskRepositoryPort.save(task);
                    }
                });
            }
            else if (batchUpdateMessage.getBatchStatus().equals(BatchStatus.FOUND)) {
                taskRepositoryPort.getById(taskId).ifPresent(task -> {
                    task.complete(batchUpdateMessage.getResult());
                    taskRepositoryPort.save(task);
                });
                stopTask(taskThread);
            }
        }
    }



    private void stopTask(UUID taskId) {
        taskThreads.get(taskId).stopTask();
    }

    private void stopTask(TaskThread taskThread) {
        taskThread.stopTask();
    }

    private void callbackBatchUpdate(BatchUpdateDto batchUpdateMessage) {
        logger.info("Batch update for task: " + batchUpdateMessage.getTaskId() + " batch: " + batchUpdateMessage.getBatchId() + " status: " + batchUpdateMessage.getBatchStatus());
        batchRepository.updateStatus(batchUpdateMessage.getTaskId(), batchUpdateMessage.getBatchId(), batchUpdateMessage.getBatchStatus());
        taskMessagePort.sendBatchUpdateMessage(batchUpdateMessage);
        if(batchUpdateMessage.getBatchStatus().equals(BatchStatus.FOUND)) {

            Task task = taskRepositoryPort.getById(batchUpdateMessage.getTaskId()).orElseThrow(() -> new RuntimeException("Task not found"));
            task.complete(batchUpdateMessage.getResult());

            taskRepositoryPort.save(task);
            doneTaskProcessor.processDoneTask(task);

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
