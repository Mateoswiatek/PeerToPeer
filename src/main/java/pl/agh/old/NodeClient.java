package pl.agh.old;

// Klient
import java.io.*;
import java.net.*;

public class NodeClient {
    private String host;
    private int port;

    public NodeClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessage(String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            System.out.println("Response: " + in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NodeClient client = new NodeClient("localhost", 5000); // adres i port serwera
        client.sendMessage("Hello from client!");
    }
}
