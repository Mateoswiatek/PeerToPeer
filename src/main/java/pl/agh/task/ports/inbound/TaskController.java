package pl.agh.task.ports.inbound;

import pl.agh.task.model.dto.*;

import java.util.Optional;
import java.util.UUID;

public interface TaskController {
    UUID createNewTask(NewTaskDto newTaskRequest);
    void updateTask(TaskUpdateMessageDto taskUpdateMessageDto);
    Optional<TaskUpdateMessageRequestDto> updateBatch(BatchUpdateDto batchUpdateMessage);
    MemoryDumpDto getMemoryDumpMessage();
    MemoryDumpDto getMemoryDumpMessage(UUID taskId);

    void updateTasks(MemoryDumpDto memoryDumpDto);
}
