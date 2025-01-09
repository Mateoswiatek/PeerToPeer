package pl.agh.network;

import pl.agh.model.Node;
import pl.agh.model.dto.request.JoinToNetworkRequest;

import java.util.UUID;

/**
 * Klasa odpowiedzialna za nasłuchiwanie na zadanym porcie oraz procesowanie informacji.
 * Tutaj będzie obłsuga nowego taska itp itd
 *
 */
public class TCPListener {
    private NetworkManager networkManager;
    private Node myself;
    private final int port;

    public TCPListener(NetworkManager networkManager, Node myself) {
        this.networkManager = networkManager;
        this.myself = myself;
        this.port = myself.getPort();
    }

//    @Async ???
    public void handleJoinConnection() {
        JoinToNetworkRequest request = new JoinToNetworkRequest(new Node(UUID.randomUUID(), "przykladIp", 5005));
        networkManager.addNewNodeToNetwork(request);
    }

}
