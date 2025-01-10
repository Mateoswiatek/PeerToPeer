package pl.agh.p2pnetwork;

import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

import java.util.HashSet;
import java.util.Set;

public class NetworkManager {
    Node myself;
    Set<Node> nodes = new HashSet<>();

    public NetworkManager(Node myself) {
        this.myself = myself;
        Thread listenerThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println("Nodes: " + nodes);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        listenerThread.start();
    }

    public void connectMyselfToNetwork(String ip, Integer port) {
        System.out.println("connect to Network invoked. ip: " + ip + ", port: " + port);
        TCPSender.sendMessage(ip, port, new JoinToNetworkRequest(myself));
    }

    public void addNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        nodes.add(joinToNetworkRequest.getNewNode());

        Set<Node> tmp = new HashSet<>(nodes);
        tmp.add(myself);
        Set<Node> disconnectedNodes = TCPSender.sendMessage(nodes, UpdateNetworkMessage.builder()
                .nodes(tmp)
                .build());

        System.out.println("disconnectedNodes: " + disconnectedNodes);
        nodes.removeAll(disconnectedNodes);
    }

    public void updateNetwork(UpdateNetworkMessage updateNetworkMessage) {
        updateNetworkMessage.getNodes().remove(myself);
        nodes.addAll(updateNetworkMessage.getNodes());
    }
}
