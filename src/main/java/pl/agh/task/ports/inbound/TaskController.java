package pl.agh.task.ports.inbound;

import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.dto.NewTaskDto;

import java.util.UUID;

public interface TaskController {
    void receiveBatchUpdateMessage(BatchUpdateDto batchUpdateMessage);
    UUID createNewTask(NewTaskDto newTaskRequest);
}
