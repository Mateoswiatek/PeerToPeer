package pl.agh.task.mapper;

import pl.agh.task.model.Task;
import pl.agh.task.model.dto.TaskUpdateMessageDto;
import pl.agh.task.ports.outbound.TaskExecutionStrategy;

public class TaskMapper {
    public static TaskUpdateMessageDto toTaskUpdateMessageDto(Task task) {
        return new TaskUpdateMessageDto(
                task.getTaskId(),
                task.getPasswordHash(),
                task.getAlphabet(),
                task.getMaxLength(),
                task.getMaxBatchSize(),
                task.getTaskStatus(),
                task.getResult());
    }

    public static Task toTask(TaskUpdateMessageDto taskUpdateMessageDto, TaskExecutionStrategy strategy) {
        return new Task(
                taskUpdateMessageDto.getTaskId(),
                taskUpdateMessageDto.getPasswordHash(),
                taskUpdateMessageDto.getAlphabet(),
                taskUpdateMessageDto.getMaxLength(),
                taskUpdateMessageDto.getMaxBatchSize(),
                taskUpdateMessageDto.getTaskStatus(),
                taskUpdateMessageDto.getResult(),
                strategy);
    }
}
