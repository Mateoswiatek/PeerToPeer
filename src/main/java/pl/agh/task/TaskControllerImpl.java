package pl.agh.task;

import lombok.AllArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.mapper.BatchMapper;
import pl.agh.mapper.TaskMapper;
import pl.agh.middleware.DoneTaskProcessor;
import pl.agh.middleware.model.MemoryDumpMessage;
import pl.agh.middleware.model.TaskFromNetworkMessage;
import pl.agh.p2pnetwork.NetworkManager;
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
    private final NetworkManager networkManager;
    private final TaskFactory taskFactory;
    private final DoneTaskProcessor doneTaskProcessor;
    private final Logger logger = Logger.getInstance();

    private final Random random = new Random();
    private final Map<UUID, TaskThread> taskThreads = new HashMap<>();

//    To ma być na zwrotce do noda ktory się podłączył.
    public MemoryDumpMessage getMemoryDump() {
        List<TaskFromNetworkMessage> tasks = taskRepositoryPort.findAll().stream().map(task -> {
            logger.info("Task ID: " + task.getTaskId());
            return TaskMapper.toTaskFromNetworkMessage(task);
        }).toList();
        List<Batch> batches = batchRepository.findAll();

        List<TaskFromNetworkMessage> doneTasks = tasks.stream().filter(t -> t.getTaskStatus().equals(TaskStatus.DONE)).toList();

        List<BatchUpdateDto> batchesUpdateDtoList = batchRepository.findAll().stream()
                .filter(b -> !b.getStatus().equals(BatchStatus.FOUND))
                .map(b -> BatchMapper.toBatchUpdateDto(b, null)).collect(toList());

        List<BatchUpdateDto> doneBatches = batches.stream().filter(b -> b.getStatus().equals(BatchStatus.FOUND)).map(b -> {
                TaskFromNetworkMessage thisTask = doneTasks.stream().filter(dt -> dt.getTaskId().equals(b.getTaskId())).findFirst().orElseThrow();
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
        // Zaktualizuj status batcha w repozytorium
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



//     * Jeśli taskId jest nullem, to zakładamy, że jest to nowe zadanie i należy je rozesłać, jeśli ma uuid, to oznacza,
//     * że pochodzi z sieci i nie musimy tego rozsyłać dalej (nie ma pętli)
    //TODO (10.01.2025): Zrobić, aby to było asynchronicznie wołane ???
    /**
     * Zawsze przychodzi nowy task
     * @param newTaskRequest
     * @return
     */
    public UUID createNewTask(NewTaskDto newTaskRequest) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy(); // Przypisanie strategii

        Task task = taskFactory.createTask(newTaskRequest, strategy);
        logger.info("Created task, id: " + task.getTaskId());

        Task saved = taskRepositoryPort.save(task);

        logger.info("Saved task: " + saved.getTaskId());

        TaskStatusLogger loggerObserver = new TaskStatusLogger();
        task.addObserver(loggerObserver);

//        CompletableFuture.runAsync(() -> taskMessagePort.sendTaskUpdateMessage(networkManager.getNodes(), saved));
        taskMessagePort.sendTaskUpdateMessage(networkManager.getNodes(), saved);
        return this.initializeBatches(saved);
    }

    public UUID createNewTaskFromNetwork(TaskFromNetworkMessage newTaskRequestFromNetwork) {
        TaskExecutionStrategy strategy = new SHA256TaskExecutionStrategy();

        Task task = taskFactory.createTask(newTaskRequestFromNetwork, strategy);
        task = taskRepositoryPort.save(task);

        TaskStatusLogger loggerObserver = new TaskStatusLogger();
        task.addObserver(loggerObserver);

//        Task finalTask = task;
//        CompletableFuture.runAsync(() -> this.initializeBatches(finalTask));
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
//        taskThreads.remove(taskId);
    }

    public void stopTask(TaskThread taskThread) {
        taskThread.stopTask();
//        taskThreads.remove(taskThread.getTaskId());
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
        taskMessagePort.sendBatchUpdateMessage(networkManager.getNodes(), batchUpdateMessage);
        if(batchUpdateMessage.getBatchStatus().equals(BatchStatus.FOUND)) {

            Task task = taskRepositoryPort.getById(batchUpdateMessage.getTaskId()).orElseThrow(() -> new RuntimeException("Task not found"));
            task.complete(batchUpdateMessage.getResult());

            taskRepositoryPort.save(task);
            doneTaskProcessor.processDoneTask(task);

            taskMessagePort.sendTaskUpdateMessage(networkManager.getNodes(), task);
            logger.info("Stop task: " + batchUpdateMessage.getTaskId());
            this.stopTask(batchUpdateMessage.getTaskId());
        }
    }
}
