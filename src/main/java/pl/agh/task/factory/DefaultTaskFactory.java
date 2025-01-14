package pl.agh.task.factory;

import pl.agh.logger.Logger;
import pl.agh.task.ports.outbound.TaskExecutionStrategy;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.dto.TaskUpdateMessageDto;
import pl.agh.task.model.enumerated.TaskStatus;

public class DefaultTaskFactory implements TaskFactory {
    private final Logger logger = Logger.getInstance();
    @Override
    public Task createNewTaskFromRequest(NewTaskDto newTaskRequest, TaskExecutionStrategy strategy) {
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
    public Task createTaskFromNetwork(TaskUpdateMessageDto taskUpdateMessageDto, TaskExecutionStrategy strategy) {
        logger.info("Task Factory - create task from network message");
        // Pobierz status z NewTaskDto lub ustaw domyślny
        TaskStatus taskStatus = taskUpdateMessageDto.getTaskStatus() != null
                ? taskUpdateMessageDto.getTaskStatus()
                : TaskStatus.CREATED;

        return new Task(
                taskUpdateMessageDto.getTaskId(),
                taskUpdateMessageDto.getPasswordHash(),
                taskUpdateMessageDto.getAlphabet(),
                taskUpdateMessageDto.getMaxLength(),
                taskUpdateMessageDto.getMaxBatchSize(),
                taskStatus,
                null,
                strategy
        );
    }
}
