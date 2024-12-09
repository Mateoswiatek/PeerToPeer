//package pl.agh.web_socket;
//
//import javax.websocket.*;
//import java.net.URI;
//
//@ClientEndpoint
//public class WebSocketClient {
//    private static final String SERVER_URI = "ws://localhost:8080/notifications";
//
//    public static void main(String[] args) {
//        try {
//            // Tworzymy połączenie WebSocket
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            container.connectToServer(WebSocketClient.class, URI.create(SERVER_URI));
//            System.out.println("Connected to server");
//
//            // Czekamy na wiadomości
//            Thread.sleep(1000000); // Czekaj na wiadomości w nieskończoność
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Metoda, która odbiera wiadomości z serwera
//    @OnMessage
//    public void onMessage(String message) {
//        System.out.println("Received from server: " + message);
//    }
//}
