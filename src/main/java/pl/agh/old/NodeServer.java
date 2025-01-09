package pl.agh.old;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeServer {
    private ServerSocket serverSocket;

//    Listener / Observer ?
//    TODO wielowątkowość - ogarnąć, aby nie było problemów z setami - usuwanie, dodawanie.
    private Set<Node> knownNodes; // Lista znanych węzłów (adres IP:port)

    public NodeServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        knownNodes = new HashSet<>();
    }

    public void addKnownNode(Node node) {
        knownNodes.add(node);
    }

    public void broadcastMessage(String message) {
        // Zbieramy węzły, które rzucają wyjątek - nie da się do nich dobić.
        Set<Node> nodesToRemove = knownNodes.stream()
                .filter(node -> {
                    try {
                        node.sendMessage(message);
                        return false; // Jeśli nie ma wyjątku, nie usuwamy węzła
                    } catch (IOException e) {
                        System.err.println("Failed to send message to: " + node.getNodeAddress());
                        return true; // Jeśli wystąpi wyjątek, zaznaczamy do usunięcia
                    }
                })
                .collect(Collectors.toSet()); // Zbieramy węzły do usunięcia do Set
        // Usuwamy węzły z knownNodes
        knownNodes.removeAll(nodesToRemove);

//        knownNodes.forEach(node -> {
//            try {
//                node.sendMessage(message);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }

    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            System.out.println("Received: " + request);

            // Tu obsłuż logikę w zależności od typu wiadomości

            out.println("Acknowledged: " + request); // To leci do wysyłającego
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        NodeServer server = new NodeServer(5000); // port serwera
        server.start();
    }
}
