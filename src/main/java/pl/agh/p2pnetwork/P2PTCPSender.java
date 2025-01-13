package pl.agh.p2pnetwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.p2pnetwork.base.P2PMessageResolver;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class P2PTCPSender {
    P2PMessageResolver messageResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     *
     * @param node
     * @param baseMessage
     * @throws IOException when something went wrong.
     */
    public void sendMessageToNode(Node node, BaseMessage baseMessage) throws IOException {
        String messageJson = messageResolver.createMessage(baseMessage);
        this.sendMessage(node.getIp(), node.getPort(), messageJson);
    }

    public Set<Node> sendMessageToNodes(Set<Node> nodes, BaseMessage baseMessage) {
        Set<Node> failedNodes = new HashSet<>();
        String messageJson = messageResolver.createMessage(baseMessage);

        nodes.forEach(node -> {
            try {
                sendMessage(node.getIp(), node.getPort(), messageJson);
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
