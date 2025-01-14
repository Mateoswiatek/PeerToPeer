package pl.agh.middleware.p2p.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TaskDumpMessageRequestMessage extends BaseMessage {
    private final UUID taskId;

    @JsonCreator
    public TaskDumpMessageRequestMessage(@JsonProperty("taskId") UUID taskId) {
        super(TaskDumpMessageRequestMessage.class.getSimpleName());
        this.taskId = taskId;
    }

}
