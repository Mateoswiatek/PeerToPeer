package pl.agh.middleware.p2p.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.agh.p2pnetwork.model.dto.BaseMessage;
import pl.agh.task.model.enumerated.TaskStatus;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TaskUpdateMessage extends BaseMessage {
    private UUID taskId;
    private String passwordHash;
    private String alphabet;
    private Long maxLength;
    private Long maxBatchSize;
    private TaskStatus taskStatus;
    private String result = "";

    @JsonCreator
    public TaskUpdateMessage(
            @JsonProperty("taskId") UUID taskId,
            @JsonProperty("passwordHash") String passwordHash,
            @JsonProperty("alphabet") String alphabet,
            @JsonProperty("maxLength") Long maxLength,
            @JsonProperty("maxBatchSize") Long maxBatchSize,
            @JsonProperty("taskStatus") TaskStatus taskStatus,
            @JsonProperty("result") String result) {
        super(TaskUpdateMessage.class.getSimpleName());
        this.taskId = taskId;
        this.passwordHash = passwordHash;
        this.alphabet = alphabet;
        this.maxLength = maxLength;
        this.maxBatchSize = maxBatchSize;
        this.taskStatus = taskStatus;
        this.result = result;
    }
}
