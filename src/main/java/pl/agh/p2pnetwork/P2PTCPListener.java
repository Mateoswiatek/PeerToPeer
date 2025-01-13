package pl.agh.p2pnetwork;

import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.base.P2PConnectionExtension;
import pl.agh.p2pnetwork.base.P2PMessageResolver;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class P2PTCPListener {
    private final NetworkManager networkManager;
    private final P2PMessageResolver messageResolver;
    private final P2PConnectionExtension p2pExtension;

    private final Logger logger = Logger.getInstance();
    private final int port;
    private boolean running;

    protected P2PTCPListener(NetworkManager networkManager, P2PMessageResolver messageResolver, P2PConnectionExtension p2pExtension, int port) {
        this.networkManager = networkManager;
        this.messageResolver = messageResolver;
        this.p2pExtension = p2pExtension;
        this.port = port;
    }

    public void startListener() {
        Thread listenerThread = new Thread(this::startInThread);
        listenerThread.start();
    }

    public void stop() {
        running = false;
        logger.info("Serwer został zatrzymany.");
    }

    private void startInThread() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Serwer nasłuchuje na porcie: " + port);
            while (running) {
                new Thread(() -> {
                    try {
                        handleClient(serverSocket.accept());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (Exception e) {
            logger.error("Błąd serwera: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        String message = "";
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            while ((message = in.readLine()) != null) {
                BaseMessage baseMessage = messageResolver.parseMessage(message);

                switch(baseMessage) {
                    case JoinToNetworkRequest joinRequest -> {
                        BaseMessage response = handleJoinToNetworkRequest(joinRequest);
                        if(response != null) {
                            out.println(messageResolver.parseToJson(handleJoinToNetworkRequest(joinRequest)));
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + baseMessage);
                }
            }
        } catch (IOException e) {
            logger.error("Błąd obsługi klienta: " + e.getMessage() + "\nMessage: " + message);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                logger.error("Błąd podczas zamykania połączenia: " + e.getMessage());
            }
        }
    }

    private BaseMessage handleJoinToNetworkRequest(JoinToNetworkRequest joinRequest) {
        logger.info("Handle join to network request - Send memory dump as response");
        networkManager.addNewNodeToNetwork(joinRequest);
        return p2pExtension.additionalActionOnNodeJoinToNetwork(joinRequest);
    }
}
