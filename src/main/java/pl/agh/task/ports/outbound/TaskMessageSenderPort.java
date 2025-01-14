package pl.agh.task.ports.outbound;

import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.dto.TaskUpdateMessageDto;

public interface TaskMessageSenderPort {
    void sendTaskUpdateMessage(TaskUpdateMessageDto taskUpdateMessage);
    void sendBatchUpdateMessage(BatchUpdateDto message);
}
