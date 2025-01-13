package pl.agh.task.ports.outbound;

import pl.agh.middleware.model.TaskUpdateMessage;
import pl.agh.task.model.dto.BatchUpdateDto;

public interface TaskMessageSenderPort {
    void sendBatchUpdateMessage(BatchUpdateDto message);
    void sendTaskUpdateMessage(TaskUpdateMessage taskUpdateMessage);
}
