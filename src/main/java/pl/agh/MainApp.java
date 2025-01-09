package pl.agh;

import pl.agh.model.Node;
import pl.agh.network.NetworkManager;
import pl.agh.network.TCPListener;

import java.util.UUID;

public class MainApp {

    public static void main(String[] args) {
        //TODO (09.01.2025): Debugging
        for(String arg : args) {
            System.out.println(arg);
        }

        if (args.length < 1) {
            throw new RuntimeException("Użycie: java pl.agh.MainApp <port> [nodeIpFromNetwork nodePortFromNetwork]");
        }

        Node myself = new Node(UUID.randomUUID(), args[0], Integer.parseInt(args[2]));

        NetworkManager networkManager = new NetworkManager(myself);
        TCPListener tCPListener = new TCPListener(networkManager, myself);

        if (args.length > 3) { // Czy podłączamy się do sieci, czy jest to pierwszy node
            networkManager.connectMyselfToNetwork(args[1], Integer.parseInt(args[2]));
        } else {
            System.out.println("Its first node in the network");
        }


    }
}
