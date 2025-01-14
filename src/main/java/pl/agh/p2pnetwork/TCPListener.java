//package pl.agh.p2pnetwork;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import pl.agh.logger.Logger;
//import pl.agh.mapper.BatchMapper;
//import pl.agh.mapper.TaskMapper;
//import pl.agh.middleware.DoneTaskProcessor;
//import pl.agh.middleware.model.BatchUpdateMessage;
//import pl.agh.middleware.model.MemoryDumpMessage;
//import pl.agh.middleware.model.NewTaskRequest;
//import pl.agh.middleware.model.TaskUpdateMessage;
//import pl.agh.task.TaskControllerImpl;
//import pl.agh.p2pnetwork.core.model.Node;
//import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
//import pl.agh.p2pnetwork.core.model.dto.message.UpdateNetworkMessage;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.UUID;
//
//import static java.lang.Thread.sleep;
//
//public class TCPListener {
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private final NetworkManager networkManager;
//    private final TaskControllerImpl taskController;
//    private final DoneTaskProcessor doneTaskProcessor;
//    private final Logger logger = Logger.getInstance();
//    private Node myself;
//    private final int port;
//    private boolean running;
//
//    public TCPListener(NetworkManager networkManager, TaskControllerImpl taskController, DoneTaskProcessor doneTaskProcessor, Node myself) {
//        this.networkManager = networkManager;
//        this.taskController = taskController;
//        this.doneTaskProcessor = doneTaskProcessor;
//        this.myself = myself;
//        this.port = myself.getPort();
//    }
//
//    public void startAsync() {
//        Thread listenerThread = new Thread(this::start);
//        listenerThread.start();
//    }
//
//
//
//    // Obsługa klienta
//    private void handleClient(Socket clientSocket) {
//        try (
//                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
//        ) {
//            String message;
//            while ((message = in.readLine()) != null) {
//                BaseMessage myMessage = MessageProcessor.parseMessage(message);
//
//                switch (myMessage) {
//                    case UpdateNetworkMessage updateMessage -> handleUpdateNetworkMessage(updateMessage);
//                    case NewTaskRequest newTaskRequest -> {
//                        UUID taskId = handleNewTaskRequest(newTaskRequest);
//                        out.println(taskId.toString());
//                    }
//                    case TaskUpdateMessage newTaskFromNetwork -> handleNewTaskFromNetwork(newTaskFromNetwork);
//                    case BatchUpdateMessage newBatchUpdateMessage -> handleBatchUpdateMessage(newBatchUpdateMessage);
//                    case MemoryDumpMessage memoryDumpMessage -> handleMemoryDumpMessage(memoryDumpMessage);
//                    default -> logger.info("Nieobsługiwany typ wiadomości: " + myMessage.getClass().getSimpleName());
//                }
//            }
//        }
//    }
//
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
//    private UUID handleNewTaskRequest(NewTaskRequest newTaskRequest) {
//        logger.info("TCPListener.handleNewTaskRequest - invoked");
//        UUID taskId = taskController.createNewTask(TaskMapper.toDto(newTaskRequest));
//        logger.info("New task id: " + taskId);
//        return taskId;
//    }
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
//
//
//}
