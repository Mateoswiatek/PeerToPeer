package pl.agh.p2pnetwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.model.dto.base.Ping;
import pl.agh.p2pnetwork.model.dto.base.UpdateNetworkMessage;
import pl.agh.p2pnetwork.ports.outbound.P2PExtension;
import pl.agh.p2pnetwork.ports.outbound.P2PMessageSerializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class NetworkManager {
    private final P2PTCPListener p2pTCPListener;
    private final P2PTCPSender p2pTCPSender;

    private final P2PExtension p2pExtension;

    Node myself;
    private Set<Node> nodesInNetwork = new HashSet<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getInstance();

    private static class P2PTCPSender {
        private final P2PMessageSerializer messageResolver;

        private P2PTCPSender(P2PMessageSerializer messageResolver) {
            this.messageResolver = messageResolver;
        }

        /**
         * @param node
         * @param baseMessage
         * @throws IOException when something went wrong.
         */
        public void sendMessageToNode(Node node, BaseMessage baseMessage) throws IOException {
            String messageJson = messageResolver.serializeMessage(baseMessage);
            this.sendMessage(node.getIp(), node.getPort(), messageJson);
        }

        public Set<Node> sendMessageToNodes(Set<Node> nodes, BaseMessage baseMessage) {
            Set<Node> failedNodes = new HashSet<>();
            String messageJson = messageResolver.serializeMessage(baseMessage);

            nodes.forEach(node -> {
                try {
                    this.sendMessage(node.getIp(), node.getPort(), messageJson);
                } catch (Exception e) {
                    System.err.println("Błąd podczas łączenia z nodem, dodajemy do nieudanych. " + e.getMessage());
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
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                while ((message = in.readLine()) != null) {
                    BaseMessage baseMessage = messageResolver.deserializeMessage(message);
                    BaseMessage response = messageHandler.apply(baseMessage);
                    if (response != null) {
                        out.println(messageResolver.serializeMessage(response));
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
    }

    public NetworkManager(Node myself, P2PMessageSerializer messageResolver, P2PExtension p2pExtension) {
        this.myself = myself;
        this.p2pExtension = p2pExtension;

        this.p2pTCPSender = new P2PTCPSender(messageResolver);
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

    public void startNetwork() {
        p2pTCPListener.startListener();
    }

    public void startNetwork(String ip, int port) {
        this.startNetwork();
        try {
            p2pTCPSender.sendMessageToNode(new Node(UUID.randomUUID(), ip, port), new JoinToNetworkRequest(myself));
        } catch (Exception e) {
            logger.error("NetworkManager.startNetwork error during connect to network. " + ip + ":" + port + " Error message: " + e.getMessage());
        }
    }

    private BaseMessage handleMessageResolver(BaseMessage baseMessage) {
        return switch (baseMessage) {
            case JoinToNetworkRequest joinRequest -> handleAddNewNodeToNetwork(joinRequest);
            case UpdateNetworkMessage updateNetworkMessage -> handleUpdateNetworkMessage(updateNetworkMessage);

            default -> p2pExtension.handleMessageOverP2P(baseMessage);
        };
    }

    /**
     * Also before start task we should call this?
     */
    private void updateNetworkPing() {
        Set<Node> disconnectedNodes = p2pTCPSender.sendMessageToNodes(nodesInNetwork, Ping.ping());
        nodesInNetwork.removeAll(disconnectedNodes);
    }

    private BaseMessage handleAddNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        // Update network
        updateNetworkPing();

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
        updateNetworkPing();
        return p2pExtension.additionalActionOnUpdateNetwork(updateNetworkMessage);
    }


//
//    public void handleAddNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
//        Node newNode = joinToNetworkRequest.getNewNode();
//
//
//
//
//
//
//
//
//
//
//
//
//        String newNodeIp = joinToNetworkRequest.getNewNode().getIp();
//        int newNodePort = joinToNetworkRequest.getNewNode().getPort();
//
//        Set<Node> duplicates = nodes.stream().filter(node ->
//                (node.getPort() == newNodePort && Objects.equals(node.getIp(), newNodeIp))).collect(Collectors.toSet());
//
//        nodes.removeAll(duplicates);
//
//        nodes.add(joinToNetworkRequest.getNewNode());
//
//        Set<Node> tmp = new HashSet<>(nodes);
//        tmp.add(myself);
//        Set<Node> disconnectedNodes = sendMessageToAllNodes(nodes, UpdateNetworkMessage.builder()
//                .nodes(tmp)
//                .build());
//
//        logger.info("disconnectedNodes: " + disconnectedNodes + " duplicates: " + duplicates);
//        nodes.removeAll(disconnectedNodes);
//    }


//    public void updateNetwork(UpdateNetworkMessage updateNetworkMessage) {
//        updateNetworkMessage.getNodes().remove(myself);
//        nodesInNetwork.addAll(updateNetworkMessage.getNodes());
//    }
//
//    private void removeFailedNode(String ip, int port) {
//        Set<Node> disconnectedNodes = nodesInNetwork.stream().filter(node ->
//                (node.getPort() == port && Objects.equals(node.getIp(), ip))).collect(Collectors.toSet());
//        if(!disconnectedNodes.isEmpty()) {
//            logger.info("Remove node: "+ disconnectedNodes);
//            this.nodesInNetwork.removeAll(disconnectedNodes);
//        }
//    }
//
//    public void sendMessage(String ip, int port, Object message) {
//        try {
//            String requestJson = objectMapper.writeValueAsString(message);
//            sendMessageRaw(ip, port, requestJson);
//            logger.info("Message sent " + ip + ":" + port + " message: " +  (requestJson.length() > 1000 ? "Message longer than 1000 chars" : requestJson));
//        } catch (JsonProcessingException e) {
//            logger.error("ip: " + ip + " port: " + port + " " + e.getMessage());
//            removeFailedNode(ip, port);
//        }
//    }
//
//    public void sendMessageRaw(String ip, int port, String jsonMessage) {
//        try (Socket socket = new Socket(ip, port)) {
//            OutputStream outputStream = socket.getOutputStream();
//            PrintWriter writer = new PrintWriter(outputStream, true);
//            writer.println(jsonMessage);
//            logger.info("Message sent " + ip + ":" + port + " message: " + (jsonMessage.length() > 1000 ? "Message longer than 1000 chars" : jsonMessage));
//        } catch (Exception e) {
//            logger.error("ip: " + ip + " port: " + port + " " + e.getMessage());
//            removeFailedNode(ip, port);
//        }
//    }
//
//    private void sendMessageRawUnhandled(String ip, int port, String jsonMessage) throws Exception {
//        Socket socket = new Socket(ip, port);
//        OutputStream outputStream = socket.getOutputStream();
//        PrintWriter writer = new PrintWriter(outputStream, true);
//        writer.println(jsonMessage);
//        logger.info("Wysłano request do " + ip + ":" + port + " message: " + jsonMessage);
//    }
//
//    public Set<Node> sendMessageToAllNodes(Set<Node> nodes, Object message) {
//        Set<Node> failedNodes = new HashSet<>();
//        String requestJson;
//        try {
//            requestJson = objectMapper.writeValueAsString(message);
//        } catch (JsonProcessingException e) {
//            logger.error("Error during writeValueAsString: " + e.getMessage());
//            return failedNodes;
//        }
//
//        nodes.forEach(node -> {
//            try {
//                sendMessageRawUnhandled(node.getIp(), node.getPort(), requestJson);
//            } catch (Exception e) {
//                logger.error("Błąd podczas łączenia z nodem, dodajemy do nieudanych. " + e.getMessage());
//                failedNodes.add(node);
//            }
//        });
//        return failedNodes;
//    }
//
//    @Override
//    public void sendBatchUpdateMessage(BatchUpdateDto message) {
//
//    }
//
//    @Override
//    public void sendTaskUpdateMessage(Task newTask) {
//
//
//    }
}



