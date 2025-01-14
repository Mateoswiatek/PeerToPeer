package pl.agh;

import pl.agh.middleware.DoneTaskProcessorToFileImpl;
import pl.agh.middleware.P2PConnectionExtensionHashImpl;
import pl.agh.middleware.P2PMessageResolverHashImpl;
import pl.agh.p2pnetwork.core.ports.outbound.P2PExtension;
import pl.agh.p2pnetwork.core.ports.outbound.P2PMessageResolver;
import pl.agh.task.TaskControllerImpl;
import pl.agh.task.factory.DefaultTaskFactory;
import pl.agh.task.impl.InMemoryBatchRepositoryAdapter;
import pl.agh.p2pnetwork.core.model.Node;
import pl.agh.p2pnetwork.NetworkManager;
import pl.agh.task.impl.InMemoryTaskRepositoryAdapter;

import java.io.IOException;
import java.util.UUID;

public class MainApp {

//    5000
//    5001 192.168.0.110 5000
//    5001 localhost 5000
    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            System.out.println(arg);
        }

        if (args.length < 1) {
            throw new RuntimeException("Użycie: java pl.agh.MainApp <port> [nodeIpFromNetwork nodePortFromNetwork]");
        }


        Node myself = new Node(UUID.randomUUID(), "localhost", Integer.parseInt(args[0]));
        P2PMessageResolver resolver = new P2PMessageResolverHashImpl();
        P2PExtension extension = new P2PConnectionExtensionHashImpl();

        DoneTaskProcessorToFileImpl doneTaskProcessorToFile = DoneTaskProcessorToFileImpl.getInstance(args[0] + ".json");

        NetworkManager networkManager = new NetworkManager(myself, resolver, extension);

//        TaskControllerImpl taskController = new TaskControllerImpl(
//                InMemoryBatchRepositoryAdapter.getInstance(),
//                InMemoryTaskRepositoryAdapter.getInstance(),
//                networkManager,
//                new DefaultTaskFactory(),
//                doneTaskProcessorToFile);

        // Czy podłączamy się do sieci, czy jest to pierwszy node
        if (args.length == 3) {
            networkManager.startNetwork(args[1], Integer.parseInt(args[2]));
        } else {
            System.out.println("Its first node in the network");
            networkManager.startNetwork();
        }
    }
}

//        String ipAddress;
//        try {
//            ipAddress = InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException("Nie można pobrać adresu IP hosta", e);
//        }
//        System.out.println("ipAddress: " + ipAddress);