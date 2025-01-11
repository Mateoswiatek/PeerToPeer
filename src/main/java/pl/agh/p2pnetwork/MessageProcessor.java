package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.model.dto.message.UpdateNetworkMessage;
import pl.agh.p2pnetwork.model.dto.request.JoinToNetworkRequest;
import pl.agh.middleware.model.NewTaskRequest;
import pl.agh.middleware.model.TaskFromNetworkMessage;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = Logger.getInstance();

    public static BaseMessage parseMessage(String jsonMessage) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);
        String type = jsonNode.get("type").asText();

        logger.info("Parse message, type: " + type + "message: " + jsonMessage);

        return switch (type) {
            case "JoinToNetworkRequest" -> objectMapper.treeToValue(jsonNode, JoinToNetworkRequest.class);
            case "UpdateNetworkMessage" -> objectMapper.treeToValue(jsonNode, UpdateNetworkMessage.class);
            case "NewTaskRequest" -> objectMapper.treeToValue(jsonNode, NewTaskRequest.class);
            case "TaskFromNetworkMessage" -> objectMapper.treeToValue(jsonNode, TaskFromNetworkMessage.class);
            case "BatchUpdateMessage" -> objectMapper.treeToValue(jsonNode, BatchUpdateMessage.class);
            default -> throw new IllegalArgumentException("Nieznany typ wiadomości: " + type);
        };
    }
}
