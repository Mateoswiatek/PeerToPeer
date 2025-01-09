package pl.agh;

import pl.agh.kernel.TaskController;
import pl.agh.kernel.TaskService;
import pl.agh.kernel.impl.InMemoryBatchRepository;
import pl.agh.model.Node;
import pl.agh.network.NetworkManager;
import pl.agh.network.TCPListener;

import java.util.UUID;

public class MainApp {

//    5000
//    5001 192.168.0.110 5000
//    5001 localhost 5000
    public static void main(String[] args) {
        //TODO (09.01.2025): Debugging
        for (String arg : args) {
            System.out.println(arg);
        }

        if (args.length < 1) {
            throw new RuntimeException("Użycie: java pl.agh.MainApp <port> [nodeIpFromNetwork nodePortFromNetwork]");
        }

        Node myself = new Node(UUID.randomUUID(), "localhost", Integer.parseInt(args[0]));
        TaskController taskController = new TaskController(new TaskService(InMemoryBatchRepository.getInstance()));
        NetworkManager networkManager = new NetworkManager(myself);
        new TCPListener(networkManager, taskController, myself).startAsync();

        if (args.length == 3) { // Czy podłączamy się do sieci, czy jest to pierwszy node
            networkManager.connectMyselfToNetwork(args[1], Integer.parseInt(args[2]));
        } else {
            System.out.println("Its first node in the network");
        }


    }
}
