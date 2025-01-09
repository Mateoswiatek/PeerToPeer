package pl.agh.p2p;
import java.io.PrintWriter;

public class MessageHandler {
    public static void handleMessage(String message, Node node, PrintWriter out) {
        // Debugowanie przetwarzanej wiadomości
        System.out.println("Processing message: " + message);

        if (message.startsWith("NEW_NODE:")) {
            String[] parts = message.split(":");
            if (parts.length < 2) {
                System.err.println("Invalid NEW_NODE message format: " + message);
                return;
            }

            String newNode = "localhost:" + parts[1];
            System.out.println("New node joined: " + newNode);

            if (!node.getConnectedPeers().contains(newNode)) {
                node.getConnectedPeers().add(newNode);
                node.broadcastNewNode(newNode);
            }

            out.println(String.join(",", node.getConnectedPeers()));

        } else if (message.startsWith("ADD_NODE:")) {
            String[] parts = message.split(":");
            if (parts.length < 2) {
                System.err.println("Invalid ADD_NODE message format: " + message);
                return;
            }

            String newNode = parts[1];
            if (!node.getConnectedPeers().contains(newNode)) {
                node.getConnectedPeers().add(newNode);
                String[] nodeParts = newNode.split(":");
                if (nodeParts.length == 2) {
                    try {
                        node.connectToPeer(nodeParts[0], Integer.parseInt(nodeParts[1]));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port in ADD_NODE message: " + newNode);
                    }
                }
            }
        } else {
            System.err.println("Unknown message type: " + message);
        }
    }
}
