package pl.agh.p2p;
import java.io.*;
import java.net.*;

public class PeerConnection implements Runnable {
    private Socket socket;
    private Node node;

    public PeerConnection(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                // Debugowanie odbieranych wiadomości
                System.out.println("Received raw message: " + message);
                MessageHandler.handleMessage(message, node, out);
            }
        } catch (IOException e) {
            System.err.println("Connection lost: " + socket.getRemoteSocketAddress());
            String disconnectedPeer = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            node.removePeer(disconnectedPeer);
        }
    }
}
