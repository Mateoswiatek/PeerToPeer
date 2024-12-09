//package pl.agh.web_socket.jetty;
//
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.websocket.api.Session;
//import org.eclipse.jetty.websocket.api.annotations.OnClose;
//import org.eclipse.jetty.websocket.api.annotations.OnError;
//import org.eclipse.jetty.websocket.api.annotations.OnMessage;
//import org.eclipse.jetty.websocket.api.annotations.OnOpen;
//import org.eclipse.jetty.websocket.api.annotations.WebSocket;
//import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
//import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.Set;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@WebSocket
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
//                client.getRemote().sendString(message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        Server server = new Server(8080);
//        WebSocketServletContextHandler context = new WebSocketServletContextHandler();
//        context.setContextPath("/");
//
//        WebSocketServletFactory factory = new WebSocketServletFactory();
//        factory.getPolicy().setMaxTextMessageSize(64 * 1024); // Opcjonalnie ustaw limit wiadomości tekstowych
//        context.addServlet(WebSocketServlet.class, "/notifications");
//        server.setHandler(context);
//
//        try {
//            // Uruchamiamy serwer
//            server.start();
//            System.out.println("Server started at ws://localhost:8080/notifications");
//            server.join(); // Czeka na zakończenie pracy serwera
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
