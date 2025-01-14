package pl.agh.p2pnetwork.core.ports.outbound;

import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.p2pnetwork.core.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.core.model.dto.request.JoinToNetworkRequest;

public interface P2PExtension {

    /**
     * When new node connecting to the network, you can handle the request for your application.
     * @return message response to the node that was connected to the network (If no response, return null)
     */
    default BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        //TODO (14.01.2025): Dorobić tutaj tworzenie dumpoa z bazy danych o naszych taskach

//        Taski i batche

        // In my implementation
        //        objectMapper.writeValueAsString(taskController.getMemoryDump());
        return null;
    }

    default BaseMessage additionalActionOnUpdateNetwork(UpdateNetworkMessage updateNetworkMessage) {
        return null;
    }




    default BaseMessage handleMessageOverP2P(BaseMessage baseMessage) {
        return null;
    }
}
