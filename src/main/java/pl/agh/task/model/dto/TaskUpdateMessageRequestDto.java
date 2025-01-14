package pl.agh.task.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TaskUpdateMessageRequestDto {
    private UUID taskId;

    public static TaskUpdateMessageRequestDto create(UUID taskId){
        return new TaskUpdateMessageRequestDto(taskId);
    }
}
