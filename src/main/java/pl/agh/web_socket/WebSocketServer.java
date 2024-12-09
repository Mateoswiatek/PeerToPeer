//package pl.agh.web_socket;
//
//
//import javax.websocket.*;
//import javax.websocket.server.ServerEndpoint;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//
//@ServerEndpoint("/notifications")
//public class WebSocketServer {
//    // Zbiór wszystkich połączonych klientów (subskrybentów)
//    private static final Set<Session> clients = new CopyOnWriteArraySet<>();
//
//    // Metoda wywoływana, gdy klient połączy się z serwerem
//    @OnOpen
//    public void onOpen(Session session) {
//        clients.add(session);
//        System.out.println("New client connected: " + session.getId());
//    }
//
//    // Metoda wywoływana, gdy klient wyśle wiadomość
//    @OnMessage
//    public void onMessage(String message, Session session) {
//        System.out.println("Received message: " + message);
//        broadcastMessage(message);
//    }
//
//    // Metoda wywoływana, gdy klient się rozłączy
//    @OnClose
//    public void onClose(Session session) {
//        clients.remove(session);
//        System.out.println("Client disconnected: " + session.getId());
//    }
//
//    // Metoda wywoływana w przypadku błędu
//    @OnError
//    public void onError(Session session, Throwable throwable) {
//        System.err.println("Error for client " + session.getId() + ": " + throwable.getMessage());
//    }
//
//    // Rozsyłanie wiadomości do wszystkich subskrybentów
//    private void broadcastMessage(String message) {
//        for (Session client : clients) {
//            try {
//                client.getBasicRemote().sendText(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        // Używamy serwera WebSocket na porcie 8080
//        org.glassfish.tyrus.server.Server server = new org.glassfish.tyrus.server.Server("localhost", 8080, "/", WebSocketServer.class);
//        try {
//            // Uruchamiamy serwer
//            server.start();
//            System.out.println("Server started at ws://localhost:8080/notifications");
//
//            // Serwer działa w nieskończoność
//            Thread.sleep(1000000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
