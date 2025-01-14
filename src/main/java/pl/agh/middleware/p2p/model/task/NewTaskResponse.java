package pl.agh.middleware.p2p.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NewTaskResponse extends BaseMessage {
    private final UUID taskId;

    @JsonCreator
    public NewTaskResponse(@JsonProperty("taskId") UUID taskId) {
        super(NewTaskResponse.class.getSimpleName());
        this.taskId = taskId;
    }

    public static NewTaskResponse create(UUID taskId) {
        return new NewTaskResponse(taskId);
    }
}
