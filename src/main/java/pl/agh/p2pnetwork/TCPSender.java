package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.Node;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TCPSender {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getInstance();

    public static void sendMessage(String ip, int port, Object message) {
        try {
            String requestJson = objectMapper.writeValueAsString(message);
            sendMessageRaw(ip, port, requestJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessageRaw(String ip, int port, String jsonMessage) {
        try (Socket socket = new Socket(ip, port)) {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println(jsonMessage);
            logger.info("Wysłano request do " + ip + ":" + port + " message: " + jsonMessage);
        } catch (Exception e) {
            logger.error("ip: " + ip + " port: " + port + " " + e.getMessage());
//            throw new RuntimeException("ip: " + ip + " port: " + port + " " + e.getMessage());
        }
    }

    public static Set<Node> sendMessageToAllNodes(Set<Node> nodes, Object message) {
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
                sendMessageRaw(node.getIp(), node.getPort(), requestJson);
            } catch (Exception e) {
                logger.error("Błąd podczas łączenia z nodem, dodajemy do nieudanych. " + e.getMessage());
                failedNodes.add(node);
            }
        });
        return failedNodes;
    }

}
