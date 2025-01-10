package pl.agh.task.ports.outbound;

import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.Task;

//TODO (10.01.2025): Zrobić implementację tego, tak aby się wysyłało na TCP oraz przyjmowanie tego, w dopiwedni sposób
public interface TaskMessageSenderPort {
    void sendBatchUpdateMessage(BatchUpdateDto message);
    void sendTaskUpdateMessage(Task newTask);
}
