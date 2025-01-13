package pl.agh.p2pnetwork.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.logger.Logger;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;

public abstract class P2PMessageResolver {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getInstance();

    public BaseMessage parseMessage(String jsonMessage) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);
        String type = jsonNode.get("type").asText();
        logger.info("P2PMessageResolver.parseMessage - invoked. Message type: " + type);

        return switch (type) {
            case "JoinToNetworkRequest" -> objectMapper.treeToValue(jsonNode, JoinToNetworkRequest.class);
            case "UpdateNetworkMessage" -> objectMapper.treeToValue(jsonNode, UpdateNetworkMessage.class);
            default -> this.messageOverP2P(jsonMessage);
        };
    }

    public String parseToJson(BaseMessage message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    public String createMessage(BaseMessage baseMessage) {
        return switch (baseMessage.getType()) {
//            case "JoinToNetworkRequest" -> this.defaultWritterAsString(baseMessage);
            default -> messageOverP2P(baseMessage);
        };
    }

    protected abstract BaseMessage messageOverP2P(String jsonMessage);
    protected String messageOverP2P(BaseMessage baseMessage) {
        String requestJson = "";
        try {
            requestJson = objectMapper.writeValueAsString(baseMessage);
        } catch (JsonProcessingException e) {
            System.err.println("P2PMessageResolver.defaultWritterAsString error:  " + e.getMessage());
        }
        return requestJson;
    }


}
