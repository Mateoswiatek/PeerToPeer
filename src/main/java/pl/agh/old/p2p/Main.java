package pl.agh.old.p2p;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter port for this node: ");
        int port = 0;

        while (port <= 0) {
            try {
                port = Integer.parseInt(scanner.nextLine().trim());
                if (port <= 0) {
                    System.out.println("Port must be a positive integer.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid port number.");
            }
        }

        Node node = new Node(port);
        node.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down node...");
            node.stop();
        }));

        while (true) {
            System.out.print("Enter peer to connect to (host:port) or 'exit' to quit: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) break;

            if (!input.contains(":")) {
                System.out.println("Invalid format. Use host:port (e.g., localhost:5001).");
                continue;
            }

            String[] parts = input.split(":");
            String host = parts[0];
            int peerPort;

            try {
                peerPort = Integer.parseInt(parts[1]);
                node.connectToPeer(host, peerPort);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Please enter a valid port number.");
            }
        }

        node.stop(); // Bezpieczne zatrzymanie węzła
        scanner.close();
    }
}
