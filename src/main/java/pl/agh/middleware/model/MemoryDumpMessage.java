package pl.agh.middleware.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.task.model.dto.BatchUpdateDto;

import java.util.List;

@Getter
@Setter
public class MemoryDumpMessage extends BaseMessage {
    List<TaskFromNetworkMessage> tasksFromNetworkMessages;
    List<BatchUpdateDto> batchUpdateDtoList;

    @JsonCreator
    public MemoryDumpMessage(
            @JsonProperty("type") String type,
            @JsonProperty("tasksFromNetworkMessages") List<TaskFromNetworkMessage> tasksFromNetworkMessages,
            @JsonProperty("batchUpdateDtoList") List<BatchUpdateDto> batchUpdateDtoList) {
        super(type);
        this.tasksFromNetworkMessages = tasksFromNetworkMessages;
        this.batchUpdateDtoList = batchUpdateDtoList;
    }
}
