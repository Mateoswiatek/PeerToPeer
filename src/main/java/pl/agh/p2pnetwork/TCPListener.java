package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.logger.Logger;
import pl.agh.mapper.BatchMapper;
import pl.agh.mapper.TaskMapper;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.model.NewTaskRequest;
import pl.agh.middleware.model.TaskFromNetworkMessage;
import pl.agh.task.TaskControllerImpl;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;
import pl.agh.task.model.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class TCPListener {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final NetworkManager networkManager;
    private final TaskControllerImpl taskController;
    private final Logger logger = Logger.getInstance();
    private Node myself;
    private final int port;
    private boolean running;

    public TCPListener(NetworkManager networkManager, TaskControllerImpl taskController, Node myself) {
        this.networkManager = networkManager;
        this.taskController = taskController;
        this.myself = myself;
        this.port = myself.getPort();
    }

    public void startAsync() {
        Thread listenerThread = new Thread(this::start);
        listenerThread.start();
    }

    private void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Serwer nasłuchuje na porcie: " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Nowe połączenie: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            logger.error("Błąd serwera: " + e.getMessage());
        }
    }


    // Metoda do zatrzymywania nasłuchiwania
    public void stop() {
        running = false;
        logger.info("Serwer został zatrzymany.");
    }

    // Obsługa klienta
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                logger.info("Handle client, message: " + message);
                BaseMessage myMessage = MessageProcessor.parseMessage(message);

                switch (myMessage) {
                    case JoinToNetworkRequest joinRequest -> {
                        String response = handleJoinToNetworkRequest(joinRequest);
                        out.println(response);
                    }
                    case UpdateNetworkMessage updateMessage -> handleUpdateNetworkMessage(updateMessage);
                    case NewTaskRequest newTaskRequest -> {
                        UUID taskId = handleNewTaskRequest(newTaskRequest);
                        out.println(taskId.toString());
                    }
                    case TaskFromNetworkMessage newTaskFromNetwork -> handleNewTaskFromNetwork(newTaskFromNetwork);
                    case BatchUpdateMessage newBatchUpdateMessage -> handleBatchUpdateMessage(newBatchUpdateMessage);
                    default -> logger.info("Nieobsługiwany typ wiadomości: " + myMessage.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            // TODO remove node, and inform network ?
            logger.error("Błąd obsługi klienta: " + e.getMessage());
        } finally {
            try {
                logger.info("Closing socket.");
                clientSocket.close();
            } catch (Exception e) {
                logger.error("Błąd podczas zamykania połączenia: " + e.getMessage());
            }
        }
    }

    private String handleJoinToNetworkRequest(JoinToNetworkRequest joinRequest) {
        logger.info("Handle join to network request - Send memory dump as response");
        networkManager.addNewNodeToNetwork(joinRequest);
        try{
            return objectMapper.writeValueAsString(taskController.getMemoryDump());
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private void handleUpdateNetworkMessage(UpdateNetworkMessage updateMessage) {
        logger.info("Handle update network message - Add new nodes: " + updateMessage.getNodes());
        networkManager.updateNetwork(updateMessage);
    }

    private UUID handleNewTaskRequest(NewTaskRequest newTaskRequest) {
        logger.info("Handle new task request");
        UUID taskId = taskController.createNewTask(TaskMapper.toDto(newTaskRequest));
        logger.info("New task id: " + taskId);
        taskController.startTask(taskId);
        return taskId;
    }

    private void handleNewTaskFromNetwork(TaskFromNetworkMessage newTaskRequestFromNetwork) {
        try {
            logger.info("Handle new task from network ...");
//            Task task = TaskMapper.toTask(newTaskRequestFromNetwork);
            UUID taskId = taskController.createNewTaskFromNetwork(newTaskRequestFromNetwork);

            logger.info("Start new task from network");
            taskController.startTask(taskId);

            logger.info("Pomyślnie obsłużono nowe zadanie z sieci: " + taskId);
        } catch (Exception e) {
            logger.error("Błąd podczas obsługi nowego zadania z sieci: " + e.getMessage());
        }
    }

    private void handleBatchUpdateMessage(BatchUpdateMessage newBatchUpdateMessage) {
        logger.info("Handle batch update message");
        taskController.receiveBatchUpdateMessage(BatchMapper.messageToBatchUpdateDto(newBatchUpdateMessage));
    }


}
