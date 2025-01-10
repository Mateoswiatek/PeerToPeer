package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.p2pnetwork.model.Node;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TCPSender {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendMessage(String ip, int port, Object message) {
        try {
            String requestJson = objectMapper.writeValueAsString(message);
            sendMessage(ip, port, requestJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String ip, int port, String jsonMessage) {
        try (Socket socket = new Socket(ip, port)) {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println(jsonMessage);
            System.out.println("Wysłano request do " + ip + ":" + port);
        } catch (Exception e) {
            throw new RuntimeException("ip: " + ip + " port: " + port + " " + e.getMessage());
        }
    }

    public static Set<Node> sendMessage(Set<Node> nodes, Object message) {
        Set<Node> failedNodes = new HashSet<>();
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            System.err.println("Error during writeValueAsString: " + e.getMessage());
            return failedNodes;
        }

        nodes.forEach(node -> {
            try {
                sendMessage(node.getIp(), node.getPort(), requestJson);
            } catch (Exception e) {
                System.err.println("Błąd podczas łączenia z nodem, dodajemy do nieudanych. " + e.getMessage());
                failedNodes.add(node);
            }
        });
        return failedNodes;
    }

}
