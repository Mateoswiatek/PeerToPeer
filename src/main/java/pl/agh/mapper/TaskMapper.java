package pl.agh.mapper;


import pl.agh.middleware.model.NewTaskRequest;
import pl.agh.middleware.model.TaskFromNetworkMessage;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.NewTaskDto;

public class TaskMapper {
    public static NewTaskDto toDto(NewTaskRequest request) {
        return NewTaskDto.builder()
                .passwordHash(request.getPasswordHash())
                .alphabet(request.getAlphabet())
                .maxLength(request.getMaxLength())
                .maxBatchSize(request.getMaxBatchSize())
                .build();
    }

    public static NewTaskRequest toRequest(NewTaskDto dto) {
        return new NewTaskRequest(
                dto.getPasswordHash(),
                dto.getAlphabet(),
                dto.getMaxLength(),
                dto.getMaxBatchSize()
        );
    }

    public static Task toTask(TaskFromNetworkMessage message) {
        return Task.builder()
                .taskId(message.getTaskId())
                .passwordHash(message.getPasswordHash())
                .alphabet(message.getAlphabet())
                .maxLength(message.getMaxLength())
                .maxBatchSize(message.getMaxBatchSize())
                .taskStatus(message.getTaskStatus())
                .result(message.getResult())
                .build();
    }

    public static TaskFromNetworkMessage toTaskFromNetworkMessage(Task task) {
        return new TaskFromNetworkMessage(
                task.getTaskId(),
                task.getPasswordHash(),
                task.getAlphabet(),
                task.getMaxLength(),
                task.getMaxBatchSize(),
                task.getTaskStatus(),
                task.getResult()
        );
    }
}
