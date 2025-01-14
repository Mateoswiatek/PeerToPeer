package pl.agh.middleware.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

import java.util.List;

@Getter
@Setter
public class MemoryDumpMessage extends BaseMessage {
    List<TaskUpdateMessage> taskUpdateMessages;
    List<BatchUpdateMessage> batchUpdateMessages;

    @JsonCreator
    public MemoryDumpMessage(@JsonProperty("taskUpdateMessages") List<TaskUpdateMessage> taskUpdateMessages,
                             @JsonProperty("batchUpdateMessages") List<BatchUpdateMessage> batchUpdateMessages) {
        super(MemoryDumpMessage.class.getSimpleName());
        this.taskUpdateMessages = taskUpdateMessages;
        this.batchUpdateMessages = batchUpdateMessages;
    }
}
