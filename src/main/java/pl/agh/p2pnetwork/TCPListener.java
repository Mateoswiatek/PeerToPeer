package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.mapper.TaskMapper;
import pl.agh.middleware.model.NewTaskRequest;
import pl.agh.middleware.model.TaskFromNetworkMessage;
import pl.agh.task.TaskControllerImpl;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

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
            System.out.println("Serwer nasłuchuje na porcie: " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowe połączenie: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        }
    }

    // Metoda do zatrzymywania nasłuchiwania
    public void stop() {
        running = false;
        System.out.println("Serwer został zatrzymany.");
    }

    // Obsługa klienta
    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
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
                    default -> System.out.println("Nieobsługiwany typ wiadomości: " + myMessage.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            System.err.println("Błąd obsługi klienta: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Błąd podczas zamykania połączenia: " + e.getMessage());
            }
        }
    }

    private String handleJoinToNetworkRequest(JoinToNetworkRequest joinRequest) {
        networkManager.addNewNodeToNetwork(joinRequest);
        try{
            return objectMapper.writeValueAsString(taskController.getMemoryDump());
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private void handleUpdateNetworkMessage(UpdateNetworkMessage updateMessage) {
        networkManager.updateNetwork(updateMessage);
    }

    private UUID handleNewTaskRequest(NewTaskRequest newTaskRequest) {
        UUID taskId = taskController.createNewTask(TaskMapper.toDto(newTaskRequest));
        taskController.startTask(taskId);
        return taskId;
    }

    private void handleNewTaskFromNetwork(TaskFromNetworkMessage newTaskRequestFromNetwork) {
        taskController.createNewTaskFromNetwork(TaskMapper.toTask(newTaskRequestFromNetwork));
    }

}
