package pl.agh.middleware.p2p;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.model.MemoryDumpMessage;
import pl.agh.middleware.p2p.model.task.NewTaskRequest;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.middleware.p2p.model.task.TaskDumpMessageRequestMessage;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.ports.outbound.P2PMessageSerializer;

public class P2PMessageResolverHashImpl extends P2PMessageSerializer {
    @Override
    protected BaseMessage deserializeMessageOverP2P(String jsonMessage) throws JsonProcessingException {
        logger.info("P2PMessageResolverHashImpl.deserializeMessageOverP2P - invoked.");

        JsonNode jsonNode = objectMapper.readTree(jsonMessage);
        String type = jsonNode.get("type").asText();
        return switch (type) {
            case "NewTaskRequest" -> objectMapper.treeToValue(jsonNode, NewTaskRequest.class);
            case "TaskUpdateMessage" -> objectMapper.treeToValue(jsonNode, TaskUpdateMessage.class);
            case "BatchUpdateMessage" -> objectMapper.treeToValue(jsonNode, BatchUpdateMessage.class);
            case "TaskDumpMessageRequestMessage" -> objectMapper.treeToValue(jsonNode, TaskDumpMessageRequestMessage.class);
            case "MemoryDumpMessage" -> objectMapper.treeToValue(jsonNode, MemoryDumpMessage.class);

            default -> throw new IllegalArgumentException("Unknown P2PMessageResolverHashImpl.deserializeMessageOverP2P. Type: " + type);
        };
    }
}

