package pl.agh.middleware.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.task.model.dto.BatchUpdateDto;

import java.util.List;

@Getter
@Setter
public class MemoryDumpMessage extends BaseMessage {
    List<TaskUpdateMessage> tasksFromNetworkMessages;
    List<BatchUpdateDto> batchUpdateDtoList;

    @JsonCreator
    public MemoryDumpMessage(
            @JsonProperty("type") String type,
            @JsonProperty("tasksFromNetworkMessages") List<TaskUpdateMessage> tasksFromNetworkMessages,
            @JsonProperty("batchUpdateDtoList") List<BatchUpdateDto> batchUpdateDtoList) {
        super(type);
        this.tasksFromNetworkMessages = tasksFromNetworkMessages;
        this.batchUpdateDtoList = batchUpdateDtoList;
    }
}
