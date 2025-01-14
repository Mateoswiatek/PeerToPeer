package pl.agh.task.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pl.agh.task.model.enumerated.TaskStatus;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TaskUpdateMessageDto {
    private UUID taskId;
    private String passwordHash;
    private String alphabet;
    private Long maxLength;
    private Long maxBatchSize;
    private TaskStatus taskStatus;
    private String result = "";
}
