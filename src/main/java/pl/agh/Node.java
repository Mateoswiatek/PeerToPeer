package pl.agh;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Data
public abstract class Node {
    protected String nodeAddress;

    protected Node(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public void sendMessage(String message) throws IOException {
        String[] parts = nodeAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
            out.println(message); // Wysyłanie wiadomości

            // Jakieś odbieranie zwrotki od noda ???
        }

    }

}
