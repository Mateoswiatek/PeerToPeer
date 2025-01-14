package pl.agh.middleware;

import lombok.Getter;
import pl.agh.logger.Logger;
import pl.agh.middleware.mapper.TaskMapper;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.middleware.p2p.model.task.NewTaskRequest;
import pl.agh.middleware.p2p.P2PMessageResolverHashImpl;
import pl.agh.middleware.p2p.model.task.NewTaskResponse;
import pl.agh.middleware.task.DoneTaskProcessor;
import pl.agh.middleware.task.DoneTaskProcessorToFileImpl;
import pl.agh.p2pnetwork.NetworkManagerImpl;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.ports.inbound.NetworkManager;
import pl.agh.p2pnetwork.ports.outbound.P2PExtension;
import pl.agh.p2pnetwork.ports.outbound.P2PMessageSerializer;
import pl.agh.task.TaskControllerImpl;
import pl.agh.task.factory.DefaultTaskFactory;
import pl.agh.task.impl.InMemoryBatchRepositoryAdapter;
import pl.agh.task.impl.InMemoryTaskRepositoryAdapter;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.dto.TaskUpdateMessageDto;
import pl.agh.task.ports.inbound.TaskController;
import pl.agh.task.ports.outbound.TaskMessageSenderPort;

import java.util.UUID;

@Getter
public class AppMiddleware implements P2PExtension, TaskMessageSenderPort {
    private static final Logger logger = Logger.getInstance();
    private final NetworkManager networkManager;
    private final TaskController taskController;

    public AppMiddleware(String[] args) {
        this.networkManager = createNetworkManager(args);
        this.taskController = createTaskController(args);
    }
    private NetworkManager createNetworkManager(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Use: java pl.agh.MainApp <port> [nodeIpFromNetwork nodePortFromNetwork]");
        }

        Node myself = new Node(UUID.randomUUID(), "localhost", Integer.parseInt(args[0]));
        P2PMessageSerializer resolver = new P2PMessageResolverHashImpl();
        return new NetworkManagerImpl(myself, resolver, this);
    }

    private TaskController createTaskController(String[] args) {
        DoneTaskProcessor doneTaskProcessorToFile = DoneTaskProcessorToFileImpl.getInstance(args[0] + ".json");

        return new TaskControllerImpl(
        InMemoryBatchRepositoryAdapter.getInstance(),
        InMemoryTaskRepositoryAdapter.getInstance(),
        this,
        new DefaultTaskFactory(),
        doneTaskProcessorToFile);
    }


    @Override
    public BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        return P2PExtension.super.additionalActionOnNodeJoinToNetwork(joinToNetworkRequest);
        //TODO (14.01.2025): Dorobić tutaj tworzenie dumpoa z bazy danych o naszych taskach

//        Taski i batche
        // In my implementation
        //        objectMapper.writeValueAsString(taskController.getMemoryDump());
    }

    @Override
    public BaseMessage handleMessageOverP2P(BaseMessage baseMessage) {
        return switch (baseMessage) {
            case NewTaskRequest newTaskRequest -> handleNewTaskRequest(newTaskRequest);
            case TaskUpdateMessage taskUpdateMessage -> handleTaskUpdateMessage(taskUpdateMessage);
//            case BatchUpdateMessage batchUpdateMessage -> handleBatchUpdateMessage(batchUpdateMessage);

            default -> throw new IllegalStateException("Unexpected value: " + baseMessage);
        };
    }

    private BaseMessage handleNewTaskRequest(NewTaskRequest newTaskRequest) {
        UUID taskId = taskController.createNewTask(TaskMapper.toNewTaskDto(newTaskRequest));

        return NewTaskResponse.create(taskId);
    }
    private BaseMessage handleTaskUpdateMessage(TaskUpdateMessage taskUpdateMessage) {
        taskController.updateTask(TaskMapper.toTaskUpdateMessageDto(taskUpdateMessage));
        return null;
    }
//    private BaseMessage handleBatchUpdateMessage(BatchUpdateMessage batchUpdateMessage) {
//        logger.info("batchUpdateMessage: " + batchUpdateMessage.toString());
//        return null;
//    }

    @Override
    public void sendTaskUpdateMessage(TaskUpdateMessageDto taskUpdateMessageDto) {
        networkManager.sendMessageToNetwork(TaskMapper.toTaskUpdateMessage(taskUpdateMessageDto));
    }

    @Override
    public void sendBatchUpdateMessage(BatchUpdateDto message) {
        logger.info("we will send BatchUpdateDto...");
//        networkManager.sendMessageToNetwork(BatchMapper.toBatchUpdateMessage(message));
    }


}


//    private UUID handleNewTaskRequest(NewTaskRequest newTaskRequest) {
//        logger.info("TCPListener.handleNewTaskRequest - invoked");
//        UUID taskId = taskController.createNewTask(TaskMapper.toDto(newTaskRequest));
//        logger.info("New task id: " + taskId);
//        return taskId;
//    }

//    private void handleMemoryDumpMessage(MemoryDumpMessage memoryDumpMessage) {
//        logger.info("Handle memory dump message");
//        logger.info("Received tasks: " + memoryDumpMessage.getTasksFromNetworkMessages().size());
//        logger.info("Received batches: " + memoryDumpMessage.getBatchUpdateDtoList().size());
//        memoryDumpMessage.getTasksFromNetworkMessages().forEach(task ->
//                handleNewTaskFromNetwork(task));
//        memoryDumpMessage.getBatchUpdateDtoList().forEach(batch ->
//                handleBatchUpdateMessage(new BatchUpdateMessage(batch)));
//    }
//
//    private void handleUpdateNetworkMessage(UpdateNetworkMessage updateMessage) {
//        logger.info("Handle update network message - Add new nodes: " + updateMessage.getNodes());
//        networkManager.updateNetwork(updateMessage);
//    }
//

//
//    private void handleNewTaskFromNetwork(TaskUpdateMessage newTaskRequestFromNetwork) {
//        try {
//            logger.info("Handle new task from network " + newTaskRequestFromNetwork.getTaskId());
//
//            if(newTaskRequestFromNetwork.getTaskId() != null) {
//                logger.info("Zapis wyniku taska. TaskId: " + newTaskRequestFromNetwork.getTaskId());
//                doneTaskProcessor.processDoneTask(newTaskRequestFromNetwork.getTask());
//            } else {
//                UUID taskId = taskController.createNewTaskFromNetwork(newTaskRequestFromNetwork);
//
//                logger.info("Start new task from network");
//                taskController.startTask(taskId);
//
//                logger.info("Pomyślnie obsłużono nowe zadanie z sieci: " + taskId);
//            }
//        } catch (Exception e) {
//            logger.error("Błąd podczas obsługi nowego zadania z sieci: " + e.getMessage());
//        }
//    }
//
//    private void handleBatchUpdateMessage(BatchUpdateMessage newBatchUpdateMessage) {
//        logger.info("Handle batch update message");
//        taskController.receiveBatchUpdateMessage(BatchMapper.messageToBatchUpdateDto(newBatchUpdateMessage));
//    }
