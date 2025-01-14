package pl.agh.middleware;

import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.p2pnetwork.core.model.dto.request.JoinToNetworkRequest;
import pl.agh.p2pnetwork.core.ports.outbound.P2PExtension;

public class P2PConnectionExtensionHashImpl implements P2PExtension {
    @Override
    public BaseMessage additionalActionOnNodeJoinToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        return P2PExtension.super.additionalActionOnNodeJoinToNetwork(joinToNetworkRequest);
    }

    @Override
    public BaseMessage handleMessageOverP2P(BaseMessage baseMessage) {
        return P2PExtension.super.handleMessageOverP2P(baseMessage);
    }
}
