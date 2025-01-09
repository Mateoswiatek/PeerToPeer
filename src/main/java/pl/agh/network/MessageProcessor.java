package pl.agh.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.model.dto.BaseMessage;
import pl.agh.model.dto.message.UpdateNetworkMessage;
import pl.agh.model.dto.request.JoinToNetworkRequest;
import pl.agh.model.dto.request.NewTaskRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static BaseMessage parseMessage(String jsonMessage) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);
        String type = jsonNode.get("type").asText();

        return switch (type) {
            case "JoinToNetworkRequest" -> objectMapper.treeToValue(jsonNode, JoinToNetworkRequest.class);
            case "UpdateNetworkMessage" -> objectMapper.treeToValue(jsonNode, UpdateNetworkMessage.class);
            case "NewTaskRequest" -> objectMapper.treeToValue(jsonNode, NewTaskRequest.class);
            default -> throw new IllegalArgumentException("Nieznany typ wiadomości: " + type);
        };
    }
}
