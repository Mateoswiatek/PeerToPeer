package pl.agh;

import java.net.*;
import java.io.*;

public class MulticastNode {
    public static void main(String[] args) throws IOException {
        InetAddress group = InetAddress.getByName("230.0.0.0");
        MulticastSocket socket = new MulticastSocket(4446);

        // Dołącz do grupy multicast
        socket.joinGroup(group);

        // Wysyłanie wiadomości
        String message = "Node: 192.168.0.1:5000";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, 4446);
        socket.send(packet);

        // Odbieranie wiadomości
        byte[] buf = new byte[256];
        DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
        socket.receive(receivedPacket);
        System.out.println("Received: " + new String(receivedPacket.getData()));

        socket.leaveGroup(group);
        socket.close();
    }
}
