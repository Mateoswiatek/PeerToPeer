package pl.agh.task.factory;

import pl.agh.task.model.Task;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.impl.TaskExecutionStrategy;

public interface TaskFactory {
    Task createTask(NewTaskDto newTaskRequest, TaskExecutionStrategy strategy);

//    Task createTask(TaskUpdateMessage newTaskRequestFromNetwork, TaskExecutionStrategy strategy);
}
