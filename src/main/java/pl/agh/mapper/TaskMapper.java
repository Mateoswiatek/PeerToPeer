package pl.agh.mapper;


import pl.agh.middleware.model.NewTaskRequest;
import pl.agh.middleware.model.TaskUpdateMessage;
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

    public static TaskUpdateMessage toTaskUpdateMessage(Task task) {
        return new TaskUpdateMessage(
                task.getTaskId(),
                task.getPasswordHash(),
                task.getAlphabet(),
                task.getMaxLength(),
                task.getMaxBatchSize(),
                task.getTaskStatus(),
                task.getResult());
    }

}
