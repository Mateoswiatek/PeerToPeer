package pl.agh.middleware.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.task.model.enumerated.BatchStatus;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BatchUpdateMessage extends BaseMessage {
    private final UUID taskId;
    private final Long batchId;
    private final BatchStatus batchStatus;
    private final String result;

    @JsonCreator
    public BatchUpdateMessage(
            @JsonProperty("taskId") java.util.UUID taskId,
            @JsonProperty("batchId") Long batchId,
            @JsonProperty("batchStatus") BatchStatus batchStatus,
            @JsonProperty("result") String result) {
        super(BatchUpdateMessage.class.getSimpleName());
        this.taskId = taskId;
        this.batchId = batchId;
        this.batchStatus = batchStatus;
        this.result = result;
    }
}
