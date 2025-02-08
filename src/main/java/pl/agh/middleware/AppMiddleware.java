package pl.agh.middleware;

import lombok.Getter;
import pl.agh.logger.Logger;
import pl.agh.middleware.mapper.BatchMapper;
import pl.agh.middleware.mapper.TaskMapper;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.model.MemoryDumpMessage;
import pl.agh.middleware.p2p.P2PMessageResolverHashImpl;
import pl.agh.middleware.p2p.model.task.NewTaskRequest;
import pl.agh.middleware.p2p.model.task.NewTaskResponse;
import pl.agh.middleware.p2p.model.task.TaskDumpMessageRequestMessage;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.middleware.task.DoneTaskProcessorToFileImpl;
import pl.agh.middleware.task.InMemoryBatchRepositoryAdapter;
import pl.agh.middleware.task.InMemoryTaskRepositoryAdapter;
import pl.agh.p2pnetwork.NetworkManagerImpl;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.ports.inbound.NetworkManager;
import pl.agh.p2pnetwork.ports.outbound.P2PExtension;
import pl.agh.p2pnetwork.ports.outbound.P2PMessageSerializer;
import pl.agh.task.TaskControllerImpl;
import pl.agh.task.factory.DefaultTaskFactory;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.dto.TaskUpdateMessageDto;
import pl.agh.task.ports.inbound.TaskController;
import pl.agh.task.ports.outbound.DoneTaskProcessor;
import pl.agh.task.ports.outbound.TaskMessageSenderPort;

import java.io.IOException;
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
        doneTaskProcessorToFile,
        false);
    }


    @Override
    public BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        try{
            networkManager.sendMessageToNode(joinToNetworkRequest.getNode(), TaskMapper.toMemoryDumpMessage(taskController.getMemoryDumpMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BaseMessage handleMessageOverP2P(BaseMessage baseMessage) {
        return switch (baseMessage) {
            case NewTaskRequest newTaskRequest -> handleNewTaskRequest(newTaskRequest);
            case TaskUpdateMessage taskUpdateMessage -> handleTaskUpdateMessage(taskUpdateMessage);
            case BatchUpdateMessage batchUpdateMessage -> handleBatchUpdateMessage(batchUpdateMessage);
            case TaskDumpMessageRequestMessage taskDumpMessageRequestMessage -> handleTaskDumpMessageRequestMessage(taskDumpMessageRequestMessage);
            case MemoryDumpMessage memoryDumpMessage -> handleMemoryDumpMessage(memoryDumpMessage);
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

    private BaseMessage handleBatchUpdateMessage(BatchUpdateMessage batchUpdateMessage) {
        taskController.updateBatch(BatchMapper.toBatchUpdateDto(batchUpdateMessage))
                .ifPresent(taskUpdateMessageRequestDto ->
                {
                    try {
                        networkManager.sendMessageToNode(batchUpdateMessage.getNode(), TaskMapper.toTaskUpdateMessageRequestMessage(taskUpdateMessageRequestDto));
                    } catch (IOException e) {
                        logger.info("Faild to send batch update request message to node " + batchUpdateMessage.getNode());
                    }
                });
        return null;
    }

    private BaseMessage handleTaskDumpMessageRequestMessage(TaskDumpMessageRequestMessage taskDumpMessageRequestMessage) {
        try{
            logger.info("handle task update message request message");
            networkManager.sendMessageToNode(taskDumpMessageRequestMessage.getNode(), TaskMapper.toMemoryDumpMessage(taskController.getMemoryDumpMessage(taskDumpMessageRequestMessage.getTaskId())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BaseMessage handleMemoryDumpMessage(MemoryDumpMessage memoryDumpMessage) {
        taskController.updateTasks(TaskMapper.toMemoryDumpDto(memoryDumpMessage));
        return null;
    }

    @Override
    public void sendTaskUpdateMessage(TaskUpdateMessageDto taskUpdateMessageDto) {
        networkManager.sendMessageToNetwork(TaskMapper.toTaskUpdateMessage(taskUpdateMessageDto));
    }

    @Override
    public void sendBatchUpdateMessage(BatchUpdateDto message) {
        logger.info("sendBatchUpdateMessage - invoked");
        networkManager.sendMessageToNetwork(BatchMapper.toTaskUpdateMessage(message));
    }

}