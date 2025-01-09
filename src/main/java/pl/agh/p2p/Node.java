package pl.agh.p2p;
import java.io.*;
import java.net.*;
import java.util.*;


public class Node {
    private int port;
    private List<String> connectedPeers = new ArrayList<>();
    private ServerSocket serverSocket;
    private boolean running = true; // Flaga sterująca

    public Node(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Node started on port: " + port);

            new Thread(() -> listenForConnections()).start();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private void listenForConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new PeerConnection(clientSocket, this)).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false; // Zatrzymujemy pętlę
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    public synchronized void connectToPeer(String host, int peerPort) {
        try {
            Socket socket = new Socket(host, peerPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Debugowanie wysyłanej wiadomości
            System.out.println("Sending NEW_NODE message: NEW_NODE:" + port);
            out.println("NEW_NODE:" + port);

            // Odbieranie listy węzłów
            String nodeList = in.readLine();
            System.out.println("Received node list: " + nodeList);

            String[] nodes = nodeList.split(",");
            for (String node : nodes) {
                if (!connectedPeers.contains(node) && !node.equals("localhost:" + port)) {
                    connectedPeers.add(node);
                    String[] parts = node.split(":");
                    connectToPeer(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to connect to peer: " + e.getMessage());
        }
    }

    public synchronized void broadcastNewNode(String newNode) {
        Iterator<String> iterator = connectedPeers.iterator();
        while (iterator.hasNext()) {
            String peer = iterator.next();
            String[] parts = peer.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println("ADD_NODE:" + newNode);
            } catch (IOException e) {
                System.err.println("Failed to send new node info to " + peer);
                iterator.remove();
            }
        }
    }

    public synchronized void removePeer(String peer) {
        connectedPeers.remove(peer);
        System.out.println("Removed peer: " + peer);
    }

    public List<String> getConnectedPeers() {
        return connectedPeers;
    }
}
