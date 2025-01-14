package pl.agh.middleware.p2p.model.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TaskUpdateMessageRequestMessage extends BaseMessage {
    private final UUID taskId;
    private final Node node;

    @JsonCreator
    public TaskUpdateMessageRequestMessage(@JsonProperty("taskId") UUID taskId,
                                           @JsonProperty("node") Node node) {
        super(TaskUpdateMessageRequestMessage.class.getSimpleName());
        this.taskId = taskId;
        this.node = node;
    }

}
