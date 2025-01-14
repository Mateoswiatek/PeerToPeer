package pl.agh.p2pnetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.model.MemoryDumpMessage;
import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.middleware.model.NewTaskRequest;
import pl.agh.middleware.model.TaskUpdateMessage;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageProcessor {




    public static BaseMessage parseMessage(String jsonMessage) throws JsonProcessingException {
        return null;

//        return switch (type) {
//            case "NewTaskRequest" -> objectMapper.treeToValue(jsonNode, NewTaskRequest.class);
//            case "TaskFromNetworkMessage" -> objectMapper.treeToValue(jsonNode, TaskUpdateMessage.class);
//            case "BatchUpdateMessage" -> objectMapper.treeToValue(jsonNode, BatchUpdateMessage.class);
//            case "MemoryDumpMessage" -> objectMapper.treeToValue(jsonNode, MemoryDumpMessage.class);
//            default -> throw new IllegalArgumentException("Nieznany typ wiadomości: " + type);
//        };
    }
}
