package pl.agh.task.factory;

import pl.agh.logger.Logger;
import pl.agh.middleware.model.TaskUpdateMessage;
import pl.agh.task.impl.TaskExecutionStrategy;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.enumerated.TaskStatus;

public class DefaultTaskFactory implements TaskFactory {
    private final Logger logger = Logger.getInstance();
    @Override
    public Task createTask(NewTaskDto newTaskRequest, TaskExecutionStrategy strategy) {
        logger.info("Task Factory - create new task");
        // Pobierz status z NewTaskDto lub ustaw domyślny
        TaskStatus taskStatus = newTaskRequest.getTaskStatus() != null
                ? newTaskRequest.getTaskStatus()
                : TaskStatus.CREATED;

        return new Task(
                null,
                newTaskRequest.getPasswordHash(),
                newTaskRequest.getAlphabet(),
                newTaskRequest.getMaxLength(),
                newTaskRequest.getMaxBatchSize(),
                taskStatus,
                null,
                strategy
        );
    }

    @Override
    public Task createTask(TaskUpdateMessage newTaskRequestFromNetwork, TaskExecutionStrategy strategy) {
        logger.info("Task Factory - create task from network message");
        // Pobierz status z NewTaskDto lub ustaw domyślny
        TaskStatus taskStatus = newTaskRequestFromNetwork.getTaskStatus() != null
                ? newTaskRequestFromNetwork.getTaskStatus()
                : TaskStatus.CREATED;

        return new Task(
                newTaskRequestFromNetwork.getTaskId(),
                newTaskRequestFromNetwork.getPasswordHash(),
                newTaskRequestFromNetwork.getAlphabet(),
                newTaskRequestFromNetwork.getMaxLength(),
                newTaskRequestFromNetwork.getMaxBatchSize(),
                taskStatus,
                null,
                strategy
        );
    }
}
