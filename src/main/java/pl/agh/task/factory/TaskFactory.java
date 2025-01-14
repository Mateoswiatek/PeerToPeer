package pl.agh.task.factory;

import pl.agh.task.model.Task;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.ports.outbound.TaskExecutionStrategy;
import pl.agh.task.model.dto.TaskUpdateMessageDto;

public interface TaskFactory {
    Task createNewTaskFromRequest(NewTaskDto newTaskRequest, TaskExecutionStrategy strategy);

    Task createTaskFromNetwork(TaskUpdateMessageDto taskUpdateMessageDto, TaskExecutionStrategy strategy);
}
