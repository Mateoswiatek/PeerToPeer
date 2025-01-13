package pl.agh.task;

import lombok.AllArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.mapper.BatchMapper;
import pl.agh.mapper.TaskMapper;
import pl.agh.middleware.DoneTaskProcessor;
import pl.agh.middleware.model.MemoryDumpMessage;
import pl.agh.middleware.model.TaskUpdateMessage;
import pl.agh.task.factory.TaskFactory;
import pl.agh.task.impl.SHA256TaskExecutionStrategy;
import pl.agh.task.impl.TaskExecutionStrategy;
import pl.agh.task.model.Batch;
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

import static java.util.stream.Collectors.toList;

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

//    To ma być na zwrotce do noda ktory się podłączył.
    public MemoryDumpMessage getMemoryDump() {
        List<TaskUpdateMessage> tasks = taskRepositoryPort.findAll().stream().map(task -> {
            logger.info("Task ID: " + task.getTaskId());
            return TaskMapper.toTaskFromNetworkMessage(task);
        }).toList();
        List<Batch> batches = batchRepository.findAll();

        List<TaskUpdateMessage> doneTasks = tasks.stream().filter(t -> t.getTaskStatus().equals(TaskStatus.DONE)).toList();

        List<BatchUpdateDto> batchesUpdateDtoList = batchRepository.findAll().stream()
                .filter(b -> !b.getStatus().equals(BatchStatus.FOUND))
                .map(b -> BatchMapper.toBatchUpdateDto(b, null)).collect(toList());

        List<BatchUpdateDto> doneBatches = batches.stream().filter(b -> b.getStatus().equals(BatchStatus.FOUND)).map(b -> {
                TaskUpdateMessage thisTask = doneTasks.stream().filter(dt -> dt.getTaskId().equals(b.getTaskId())).findFirst().orElseThrow();
                return BatchMapper.toBatchUpdateDto(b, thisTask.getResult());
            }).toList();

        batchesUpdateDtoList.addAll(doneBatches);

        return new MemoryDumpMessage("MemoryDumpMessage", tasks, batchesUpdateDtoList);
    }
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

    /**
     * Zawsze przychodzi nowy task od Klienta.
     * @param newTaskRequest request from Client / external system
     * @return taskId
     */
    public UUID createNewTask(NewTaskDto newTaskRequest) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();

        Task task = taskFactory.createTask(newTaskRequest, strategy);
        task.addObserver(new TaskStatusLogger());

        Task saved = taskRepositoryPort.save(task);
        logger.info("Saved task: " + saved.getTaskId());

        taskMessagePort.sendTaskUpdateMessage(TaskMapper.toTaskUpdateMessage(task));
        this.initializeBatches(saved);
        this.startTask(saved.getTaskId());
        return saved.getTaskId();
    }

    public UUID createNewTaskFromNetwork(TaskUpdateMessage newTaskRequestFromNetwork) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();

        Task task = taskFactory.createTask(newTaskRequestFromNetwork, strategy);
        task = taskRepositoryPort.save(task);

        TaskStatusLogger loggerObserver = new TaskStatusLogger();
        task.addObserver(loggerObserver);

        return this.initializeBatches(task);
    }

    private UUID initializeBatches(Task task) {
        logger.info("Initialize Batches for task: " + task.getTaskId());
        BigInteger totalPermutations = calculateTotalPermutations(task.getAlphabet().length(), task.getMaxLength());

        List<Batch> batches = new ArrayList<>();
        BigInteger currentMin = BigInteger.ZERO;
        BigInteger maxBatchSize = BigInteger.valueOf(task.getMaxBatchSize());

        UUID taskId = task.getTaskId();
        Long batchId = 1L;
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
        return taskId;
    }

    public void startTask(UUID taskId) {
        logger.info("Start new  task: " + taskId);
        taskThreads.computeIfAbsent(
                taskId,
                newThreadTaskId -> {
                    TaskThread taskThread = new TaskThread(
                            this::getNextBatch,
                            this::callbackBatchUpdate,
                            id -> taskRepositoryPort.getById(id)
                                    .orElseThrow(() -> new RuntimeException("Task not found: " + id)), // Funkcja dostarczająca Task
                            taskId // ID zadania
                    );

                    Thread thread = new Thread(taskThread);
                    thread.start();
                    return taskThread;
                });
    }

    public void stopTask(UUID taskId) {
        taskThreads.get(taskId).stopTask();
    }

    public void stopTask(TaskThread taskThread) {
        taskThread.stopTask();
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

    private void callbackBatchUpdate(BatchUpdateDto batchUpdateMessage) {
        logger.info("Batch update for task: " + batchUpdateMessage.getTaskId() + " batch: " + batchUpdateMessage.getBatchId() + " status: " + batchUpdateMessage.getBatchStatus());
        batchRepository.updateStatus(batchUpdateMessage.getTaskId(), batchUpdateMessage.getBatchId(), batchUpdateMessage.getBatchStatus());
        taskMessagePort.sendBatchUpdateMessage(batchUpdateMessage);
        if(batchUpdateMessage.getBatchStatus().equals(BatchStatus.FOUND)) {

            Task task = taskRepositoryPort.getById(batchUpdateMessage.getTaskId()).orElseThrow(() -> new RuntimeException("Task not found"));
            task.complete(batchUpdateMessage.getResult());

            taskRepositoryPort.save(task);
            doneTaskProcessor.processDoneTask(task);

            taskMessagePort.sendTaskUpdateMessage(task);
            logger.info("Stop task: " + batchUpdateMessage.getTaskId());
            this.stopTask(batchUpdateMessage.getTaskId());
        }
    }
}
