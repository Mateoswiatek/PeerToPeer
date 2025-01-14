package pl.agh;

import pl.agh.middleware.AppMiddleware;
import pl.agh.p2pnetwork.ports.inbound.NetworkManager;

public class MainApp {

    public static void main(String[] args) {
        AppMiddleware appMiddleware = new AppMiddleware(args);
        NetworkManager networkManager = appMiddleware.getNetworkManager();
//        TaskController taskController = appMiddleware.getTaskController();


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