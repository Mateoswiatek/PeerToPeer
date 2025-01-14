package pl.agh.p2pnetwork.ports.outbound;

import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.model.dto.base.UpdateNetworkMessage;

public interface P2PExtension {

    /**
     * When new node connecting to the network, you can handle the request for your application.
     *
     * @return message response to the node that was connected to the network (If no response, return null)
     */
    default BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        return null;
    }

    default BaseMessage additionalActionOnUpdateNetwork(UpdateNetworkMessage updateNetworkMessage) {
        return null;
    }

    /**
     * @param baseMessage other messages from the system built on the P2P network
     * @return return response if bidirectional (otherwise return null). You have to add your model to {@link P2PMessageSerializer}.
     * @see P2PMessageSerializer
     */
    default BaseMessage handleMessageOverP2P(BaseMessage baseMessage) {
        return null;
    }
}



