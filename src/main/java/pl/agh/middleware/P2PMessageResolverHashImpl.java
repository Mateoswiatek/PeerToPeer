package pl.agh.middleware;

import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.p2pnetwork.core.ports.outbound.P2PMessageResolver;

public class P2PMessageResolverHashImpl extends P2PMessageResolver {
    @Override
    protected BaseMessage deserializeMessageOverP2P(String jsonMessage) {
        System.out.println("jsonMessage: " + jsonMessage);
        return null;
    }
}
