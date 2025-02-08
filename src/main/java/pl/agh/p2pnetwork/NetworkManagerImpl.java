package pl.agh.p2pnetwork;

import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.model.dto.base.Ping;
import pl.agh.p2pnetwork.model.dto.base.UpdateNetworkMessage;
import pl.agh.p2pnetwork.ports.inbound.NetworkManager;
import pl.agh.p2pnetwork.ports.outbound.P2PExtension;
import pl.agh.p2pnetwork.ports.outbound.P2PMessageSerializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class NetworkManagerImpl implements NetworkManager {
    private static final Logger logger = Logger.getInstance();
    private final P2PTCPListener p2pTCPListener;
    private final P2PTCPSender p2pTCPSender;

    private final P2PExtension p2pExtension;

    Node myself;
    private final Set<Node> nodesInNetwork = new HashSet<>();

    private static class P2PTCPSender {
        private static final Logger logger = Logger.getInstance();
        private final P2PMessageSerializer messageResolver;
        Node myself;

        private P2PTCPSender(P2PMessageSerializer messageResolver, Node myself) {
            this.messageResolver = messageResolver;
            this.myself = myself;
        }

        /**
         * @param node
         * @param baseMessage
         * @throws IOException when something went wrong.
         */
        public void sendMessageToNode(Node node, BaseMessage baseMessage) throws IOException {
            baseMessage.setNode(myself);
            String messageJson = messageResolver.serializeMessage(baseMessage);
            this.sendMessage(node.getIp(), node.getPort(), messageJson);
        }

        public Set<Node> sendMessageToNodes(Set<Node> nodes, BaseMessage baseMessage) {
            baseMessage.setNode(myself);
            Set<Node> failedNodes = new HashSet<>();
            String messageJson = messageResolver.serializeMessage(baseMessage);
            nodes.forEach(node -> {
                try {
                    this.sendMessage(node.getIp(), node.getPort(), messageJson);
                } catch (Exception e) {
                    logger.error("sendMessageToNodes - can not connect to node. Remove node: " + node.getIp() + ":" + node.getPort() + " Error: " + e.getMessage());
                    failedNodes.add(node);
                }
            });
            return failedNodes;
        }

        private void sendMessage(String ip, int port, String requestJson) throws IOException {
            try (Socket socket = new Socket(ip, port);
                 OutputStream outputStream = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(outputStream, true)) {
                writer.println(requestJson);
            } catch (IOException e) {
                throw new IOException("Błąd podczas wysyłania wiadomości do " + ip + ":" + port, e);
            }
        }
    }

    private static class P2PTCPListener {
        private final P2PMessageSerializer messageResolver;
        private final UnaryOperator<BaseMessage> messageHandler;

        private final Logger logger = Logger.getInstance();
        private final int port;
        private boolean running;

        private P2PTCPListener(P2PMessageSerializer messageResolver, UnaryOperator<BaseMessage> messageHandler, int port) {
            this.messageResolver = messageResolver;
            this.messageHandler = messageHandler;
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
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(() -> handleClient(clientSocket)).start();
                    } catch (IOException e) {
                        if (running) {
                            logger.error("Błąd podczas akceptacji połączenia: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Błąd serwera: " + e.getMessage());
            }
        }

        private void handleClient(Socket clientSocket) {
            String message = "";
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    //TODO (14.01.2025): Coś z dwustronną komunikacją nie działa jak należy :'(
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                while ((message = in.readLine()) != null) {
                    BaseMessage baseMessage = messageResolver.deserializeMessage(message);
                    BaseMessage response = messageHandler.apply(baseMessage);
                    //TODO (08.02.2025): Zamienić to na Optionala zamiast zwracać i sprawdzać nulle.
//                    if (response != null) {
//                        logger.info("wysyłamy wiadomość w nowym watku!!!");
//                        new Thread(() -> out.println(messageResolver.serializeMessage(response))).start();
//                    }
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
    }

    public NetworkManagerImpl(Node myself, P2PMessageSerializer messageResolver, P2PExtension p2pExtension) {
        this.myself = myself;
        this.p2pExtension = p2pExtension;

        this.p2pTCPSender = new P2PTCPSender(messageResolver, myself);
        this.p2pTCPListener = new P2PTCPListener(messageResolver, this::handleMessageResolver, myself.getPort());

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    logger.info("Nodes: " + nodesInNetwork);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void startNetwork() {p2pTCPListener.startListener();}

    @Override
    public void startNetwork(String ip, int port) {
        this.startNetwork();
        try {
            p2pTCPSender.sendMessageToNode(new Node(UUID.randomUUID(), ip, port), new JoinToNetworkRequest(myself));
        } catch (Exception e) {
            logger.error("NetworkManager.startNetwork error during connect to network. " + ip + ":" + port + " Error message: " + e.getMessage());
        }
    }

    @Override
    public void stopNetwork() {p2pTCPListener.stop();}

    @Override
    public void updateNetwork() {
        Set<Node> disconnectedNodes = p2pTCPSender.sendMessageToNodes(nodesInNetwork, Ping.ping());
        nodesInNetwork.removeAll(disconnectedNodes);
    }

    @Override
    public void sendMessageToNetwork(BaseMessage message) {
        updateNetwork();
        p2pTCPSender.sendMessageToNodes(nodesInNetwork, message);
    }

    @Override
    public void sendMessageToNode(Node node, BaseMessage message) throws IOException {
        p2pTCPSender.sendMessageToNode(node, message);
    }

    private BaseMessage handleMessageResolver(BaseMessage baseMessage) {
        return switch (baseMessage) {
            case JoinToNetworkRequest joinRequest -> handleAddNewNodeToNetwork(joinRequest);
            case UpdateNetworkMessage updateNetworkMessage -> handleUpdateNetworkMessage(updateNetworkMessage);
            case Ping ping -> handlePing(ping);
            default -> p2pExtension.handleMessageOverP2P(baseMessage);
        };
    }
    private BaseMessage handlePing(Ping ping) {
//        logger.info("Ping: " + ping);
        return null;
    }

    private BaseMessage handleAddNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        // Update network
        updateNetwork();

        //add new node
        nodesInNetwork.add(joinToNetworkRequest.getNewNode());

        Set<Node> networkForOther = new HashSet<>(nodesInNetwork);
        networkForOther.add(myself);
        p2pTCPSender.sendMessageToNodes(nodesInNetwork, UpdateNetworkMessage.builder().nodes(networkForOther).build());

        return p2pExtension.additionalActionOnNodeJoinToNetwork(joinToNetworkRequest);
    }

    private BaseMessage handleUpdateNetworkMessage(UpdateNetworkMessage updateNetworkMessage) {
        Set<Node> newNodes = updateNetworkMessage.getNodes();
        newNodes.remove(myself);

        nodesInNetwork.addAll(newNodes);
        updateNetwork();
        return p2pExtension.additionalActionOnUpdateNetwork(updateNetworkMessage);
    }
}



