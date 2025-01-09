package pl.agh.old;

import java.io.*;
import java.net.*;
import java.util.*;



public class NotificationServer {
    private static final int PORT = 5000;
    private static final List<PrintWriter> subscribers = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running, waiting for subscribers...");

            // Obsługuje nowych subskrybentów
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleSubscriber(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Nasłuchuje na porcie, a gdy przyjdzie wiadomość, przekazuje ją do subskrybentów
            while (true) {
                String message = readMessageFromClient();
                if (message != null) {
                    notifySubscribers(message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Odczytuje wiadomość od klienta
    private static String readMessageFromClient() {
        // Przykład prostego odczytu z konsoli, w rzeczywistości można używać klienta nasłuchującego na porcie
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obsługuje nowych subskrybentów
    private static void handleSubscriber(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            subscribers.add(out);
            System.out.println("New subscriber connected");

            // Wysyła powiadomienia do subskrybenta, gdy serwer ma nową wiadomość
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message;
                    while ((message = in.readLine()) != null) {
                        // Można dodać logikę do przetwarzania wiadomości od subskrybenta
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Powiadamia wszystkich subskrybentów
    private static void notifySubscribers(String message) {
        for (PrintWriter subscriber : subscribers) {
            subscriber.println(message);
        }
    }
}
