package pl.agh.p2pnetwork.base;

import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

public interface P2PConnectionExtension {

    /**
     * When new node connecting to the network, you can handle the request for your application.
     * @return message response to the node that was connected to the network (If no response, return null)
     */
    default BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        // In my implementation
        //        objectMapper.writeValueAsString(taskController.getMemoryDump());
        return null;
    }
    default void processResponseForNodeJoinToNetwork(BaseMessage baseMessage) {

    }

}
