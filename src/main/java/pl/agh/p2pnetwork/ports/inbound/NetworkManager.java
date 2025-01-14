package pl.agh.p2pnetwork.ports.inbound;

import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

public interface NetworkManager {
    void startNetwork();
    void startNetwork(String ip, int port);
    void stopNetwork();
    void updateNetwork();
    Node getMyself();

    void sendMessageToNetwork(BaseMessage message);
}
