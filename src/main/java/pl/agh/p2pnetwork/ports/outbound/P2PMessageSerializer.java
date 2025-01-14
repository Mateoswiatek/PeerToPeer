package pl.agh.p2pnetwork.ports.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.base.JoinToNetworkRequest;
import pl.agh.p2pnetwork.model.dto.base.Ping;
import pl.agh.p2pnetwork.model.dto.base.UpdateNetworkMessage;

public abstract class P2PMessageSerializer {
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final Logger logger = Logger.getInstance();

    public BaseMessage deserializeMessage(String jsonMessage) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);
        String type = jsonNode.get("type").asText();
        logger.info("P2PMessageResolver.deserializeMessage - invoked. Message type: " + type);

        return switch (type) {
            case "JoinToNetworkRequest" -> objectMapper.treeToValue(jsonNode, JoinToNetworkRequest.class);
            case "UpdateNetworkMessage" -> objectMapper.treeToValue(jsonNode, UpdateNetworkMessage.class);
            case "Ping" -> objectMapper.treeToValue(jsonNode, Ping.class);
            default -> this.deserializeMessageOverP2P(jsonMessage);
        };
    }

    public String serializeMessage(BaseMessage baseMessage) {
        return switch (baseMessage.getType()) {
//            case "JoinToNetworkRequest" -> this.defaultWritterAsString(baseMessage);
            default -> serializeMessageOverP2P(baseMessage);
        };
    }

    protected abstract BaseMessage deserializeMessageOverP2P(String jsonMessage) throws JsonProcessingException;

    protected String serializeMessageOverP2P(BaseMessage baseMessage) {
        String requestJson = "";
        try {
            requestJson = objectMapper.writeValueAsString(baseMessage);
        } catch (JsonProcessingException e) {
            System.err.println("P2PMessageResolver.defaultWritterAsString error:  " + e.getMessage());
        }
        return requestJson;
    }
}
