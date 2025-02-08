package pl.agh;

import pl.agh.logger.Logger;
import pl.agh.middleware.AppMiddleware;
import pl.agh.p2pnetwork.ports.inbound.NetworkManager;

public class MainApp {

    public static void main(String[] args) {
        Logger.getInstance(args[0]);

        AppMiddleware appMiddleware = new AppMiddleware(args);
        NetworkManager networkManager = appMiddleware.getNetworkManager();

        // Czy podłączamy się do sieci, czy jest to pierwszy node
        if (args.length == 3) {
            networkManager.startNetwork(args[1], Integer.parseInt(args[2]));
        } else {
            System.out.println("Its first node in the network");
            networkManager.startNetwork();
        }
    }
}