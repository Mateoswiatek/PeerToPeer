package pl.agh.p2pnetwork;

import lombok.Getter;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

import java.util.HashSet;
import java.util.Set;

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

        logger.info("New node joins the network, check current network status");
        Set<Node> tmp = new HashSet<>(nodes);
        tmp.add(myself);

        Set<Node> disconnectedNodes = TCPSender.sendMessageToAllNodes(nodes, UpdateNetworkMessage.builder()
                .nodes(tmp)
                .build());

        logger.info("Remove disconnected nodes: " + disconnectedNodes);
        nodes.removeAll(disconnectedNodes);

        logger.info("Send actual network status to all nodes, including the new one");
        nodes.add(joinToNetworkRequest.getNewNode());
        tmp.removeAll(disconnectedNodes);
        tmp.add(joinToNetworkRequest.getNewNode());
        TCPSender.sendMessageToAllNodes(nodes, UpdateNetworkMessage.builder()
                .nodes(tmp).build());
    }

    public void updateNetwork(UpdateNetworkMessage updateNetworkMessage) {
        updateNetworkMessage.getNodes().remove(myself);
        nodes.addAll(updateNetworkMessage.getNodes());
    }
}
