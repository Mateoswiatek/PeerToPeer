package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.BatchUpdateDto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NetworkManager {
    Node myself;
    @Getter
    Set<Node> nodes = new HashSet<>();
    private final P2PTCPListener p2pTCPListener;
    private final P2PTCPSender p2pTCPSender;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getInstance();

    public NetworkManager(Node myself, P2PTCPListener p2pTCPListener, P2PTCPSender p2pTCPSender) {
        this.myself = myself;
        this.p2pTCPListener = p2pTCPListener;
        this.p2pTCPSender = p2pTCPSender;

        Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    logger.info("Nodes: " + nodes);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        listenerThread.start();
    }

    public void connectMyselfToNetwork(String ip, int port) throws IOException {
        logger.info("connect to Network invoked. ip: " + ip + ", port: " + port);
        p2pTCPSender.sendJoinToNetworkRequest(ip, port, new JoinToNetworkRequest(myself));
    }


//
//    public void connectMyselfToNetwork(String ip, Integer port) {
//        sendMessage(ip, port, new JoinToNetworkRequest(myself));
//    }

    public void addNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        Node newNode = joinToNetworkRequest.getNewNode();












        String newNodeIp = joinToNetworkRequest.getNewNode().getIp();
        int newNodePort = joinToNetworkRequest.getNewNode().getPort();

        Set<Node> duplicates = nodes.stream().filter(node ->
                (node.getPort() == newNodePort && Objects.equals(node.getIp(), newNodeIp))).collect(Collectors.toSet());

        nodes.removeAll(duplicates);

        nodes.add(joinToNetworkRequest.getNewNode());

        Set<Node> tmp = new HashSet<>(nodes);
        tmp.add(myself);
        Set<Node> disconnectedNodes = sendMessageToAllNodes(nodes, UpdateNetworkMessage.builder()
                .nodes(tmp)
                .build());

        logger.info("disconnectedNodes: " + disconnectedNodes + " duplicates: " + duplicates);
        nodes.removeAll(disconnectedNodes);
    }

    public void updateNetwork(UpdateNetworkMessage updateNetworkMessage) {
        updateNetworkMessage.getNodes().remove(myself);
        nodes.addAll(updateNetworkMessage.getNodes());
    }

    private void removeFailedNode(String ip, int port) {
        Set<Node> disconnectedNodes = nodes.stream().filter(node ->
                (node.getPort() == port && Objects.equals(node.getIp(), ip))).collect(Collectors.toSet());
        if(!disconnectedNodes.isEmpty()) {
            logger.info("Remove node: "+ disconnectedNodes);
            this.nodes.removeAll(disconnectedNodes);
        }
    }

    public void sendMessage(String ip, int port, Object message) {
        try {
            String requestJson = objectMapper.writeValueAsString(message);
            sendMessageRaw(ip, port, requestJson);
            logger.info("Message sent " + ip + ":" + port + " message: " +  (requestJson.length() > 1000 ? "Message longer than 1000 chars" : requestJson));
        } catch (JsonProcessingException e) {
            logger.error("ip: " + ip + " port: " + port + " " + e.getMessage());
            removeFailedNode(ip, port);
        }
    }

    public void sendMessageRaw(String ip, int port, String jsonMessage) {
        try (Socket socket = new Socket(ip, port)) {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println(jsonMessage);
            logger.info("Message sent " + ip + ":" + port + " message: " + (jsonMessage.length() > 1000 ? "Message longer than 1000 chars" : jsonMessage));
        } catch (Exception e) {
            logger.error("ip: " + ip + " port: " + port + " " + e.getMessage());
            removeFailedNode(ip, port);
        }
    }

    private void sendMessageRawUnhandled(String ip, int port, String jsonMessage) throws Exception {
        Socket socket = new Socket(ip, port);
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(jsonMessage);
        logger.info("Wysłano request do " + ip + ":" + port + " message: " + jsonMessage);
    }

    public Set<Node> sendMessageToAllNodes(Set<Node> nodes, Object message) {
        Set<Node> failedNodes = new HashSet<>();
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.error("Error during writeValueAsString: " + e.getMessage());
            return failedNodes;
        }

        nodes.forEach(node -> {
            try {
                sendMessageRawUnhandled(node.getIp(), node.getPort(), requestJson);
            } catch (Exception e) {
                logger.error("Błąd podczas łączenia z nodem, dodajemy do nieudanych. " + e.getMessage());
                failedNodes.add(node);
            }
        });
        return failedNodes;
    }

    @Override
    public void sendBatchUpdateMessage(BatchUpdateDto message) {

    }

    @Override
    public void sendTaskUpdateMessage(Task newTask) {


    }
}
