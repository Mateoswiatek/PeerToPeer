package pl.agh.task.ports.inbound;

import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.dto.TaskUpdateMessageDto;

import java.util.UUID;

public interface TaskController {
    UUID createNewTask(NewTaskDto newTaskRequest);
    void updateTask(TaskUpdateMessageDto taskUpdateMessageDto);
    void receiveBatchUpdateMessage(BatchUpdateDto batchUpdateMessage);
}
