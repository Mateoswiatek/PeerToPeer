package pl.agh.middleware;

import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.p2pnetwork.ports.outbound.P2PMessageSerializer;

public class P2PMessageResolverHashImpl extends P2PMessageSerializer {
    @Override
    protected BaseMessage deserializeMessageOverP2P(String jsonMessage) {
        System.out.println("jsonMessage: " + jsonMessage);
        return null;
    }
}


//            case "NewTaskRequest" -> objectMapper.treeToValue(jsonNode, NewTaskRequest.class);
//            case "TaskFromNetworkMessage" -> objectMapper.treeToValue(jsonNode, TaskUpdateMessage.class);
//            case "BatchUpdateMessage" -> objectMapper.treeToValue(jsonNode, BatchUpdateMessage.class);
//            case "MemoryDumpMessage" -> objectMapper.treeToValue(jsonNode, MemoryDumpMessage.class);
