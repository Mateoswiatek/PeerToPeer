package pl.agh.task.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NewTaskDto {
    private final String passwordHash;
    private final String alphabet;
    private final Long maxLength;
    private final Long maxBatchSize;
}
