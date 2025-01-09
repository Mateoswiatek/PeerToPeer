package pl.agh.network;

import pl.agh.kernel.TaskController;
import pl.agh.model.Node;
import pl.agh.model.dto.BaseMessage;
import pl.agh.model.dto.message.UpdateNetworkMessage;
import pl.agh.model.dto.request.JoinToNetworkRequest;
import pl.agh.model.dto.request.NewTaskRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class TCPListener {
    private final NetworkManager networkManager;
    private final TaskController taskController;
    private Node myself;
    private final int port;
    private boolean running;

    public TCPListener(NetworkManager networkManager, TaskController taskController, Node myself) {
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
                    case JoinToNetworkRequest joinRequest -> handleJoinToNetworkRequest(joinRequest);
                    case UpdateNetworkMessage updateMessage -> handleUpdateNetworkMessage(updateMessage);
                    case NewTaskRequest newTaskRequest -> {
                        UUID taskId = handleNewTaskRequest(newTaskRequest);
                        out.println(taskId.toString());
                    }
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

    private void handleJoinToNetworkRequest(JoinToNetworkRequest joinRequest) {
        networkManager.addNewNodeToNetwork(joinRequest);
    }

    private void handleUpdateNetworkMessage(UpdateNetworkMessage updateMessage) {
        networkManager.updateNetwork(updateMessage);
    }

    private UUID handleNewTaskRequest(NewTaskRequest newTaskRequest) {
        return taskController.createNewTask(newTaskRequest);
    }

}
