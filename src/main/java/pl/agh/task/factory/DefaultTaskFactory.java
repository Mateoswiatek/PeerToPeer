package pl.agh.task.factory;

import pl.agh.task.impl.TaskExecutionStrategy;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.enumerated.TaskStatus;

import java.util.UUID;

public class DefaultTaskFactory implements TaskFactory {
    @Override
    public Task createTask(NewTaskDto newTaskRequest, TaskExecutionStrategy strategy) {
        // Pobierz status z NewTaskDto lub ustaw domyślny
        TaskStatus taskStatus = newTaskRequest.getTaskStatus() != null
                ? newTaskRequest.getTaskStatus()
                : TaskStatus.CREATED;

        return Task.builder()
                .taskId(UUID.randomUUID()) // Generowanie unikalnego ID dla nowego taska
                .passwordHash(newTaskRequest.getPasswordHash())
                .alphabet(newTaskRequest.getAlphabet())
                .maxLength(newTaskRequest.getMaxLength())
                .maxBatchSize(newTaskRequest.getMaxBatchSize())
                .taskStatus(taskStatus)
                .strategy(strategy)
                .build();
    }
}
