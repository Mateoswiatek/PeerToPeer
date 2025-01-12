package pl.agh.p2pnetwork;

import lombok.Getter;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class NetworkManager {
    Node myself;
    @Getter
    Set<Node> nodes = new HashSet<>();
    
    Logger logger = Logger.getInstance();

    public NetworkManager(Node myself) {
        this.myself = myself;
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

    public void connectMyselfToNetwork(String ip, Integer port) {
        logger.info("connect to Network invoked. ip: " + ip + ", port: " + port);
        TCPSender.sendMessage(ip, port, new JoinToNetworkRequest(myself));
    }

    public void addNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {

        String newNodeIp = joinToNetworkRequest.getNewNode().getIp();
        int newNodePort = joinToNetworkRequest.getNewNode().getPort();

        logger.info("New node joins the network, check current network status");

        Set<Node> duplicates = nodes.stream().filter(node ->
                (node.getPort() == newNodePort && Objects.equals(node.getIp(), newNodeIp))).collect(Collectors.toSet());

        nodes.removeAll(duplicates);

        nodes.add(joinToNetworkRequest.getNewNode());

        Set<Node> tmp = new HashSet<>(nodes);
        tmp.add(myself);
        Set<Node> disconnectedNodes = TCPSender.sendMessageToAllNodes(nodes, UpdateNetworkMessage.builder()
                .nodes(tmp)
                .build());

        logger.info("disconnectedNodes: " + disconnectedNodes + " duplicates: " + duplicates);
        nodes.removeAll(disconnectedNodes);
    }

    public void updateNetwork(UpdateNetworkMessage updateNetworkMessage) {
        updateNetworkMessage.getNodes().remove(myself);
        nodes.addAll(updateNetworkMessage.getNodes());
    }
}
