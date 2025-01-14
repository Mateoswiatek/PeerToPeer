package pl.agh.task.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import pl.agh.task.model.Batch;
import pl.agh.task.model.enumerated.BatchStatus;

import java.util.UUID;

@Data
@Builder
public class BatchUpdateDto {
    private UUID taskId;
    private Long batchId;
    private BatchStatus batchStatus;
    private String result;

    public BatchUpdateDto(
            UUID taskId,
            Long batchId,
            BatchStatus batchStatus,
            String result) {
        this.taskId = taskId;
        this.batchId = batchId;
        this.batchStatus = batchStatus;
        this.result = result;
    }

    public static BatchUpdateDto getFromBatchWithStatus(Batch batch, BatchStatus newStatus) {
        return BatchUpdateDto.builder()
                .taskId(batch.getTaskId())
                .batchId(batch.getBatchId())
                .batchStatus(newStatus)
                .build();
    }

    public static BatchUpdateDto completeTask(Batch batch, String result) {
        return BatchUpdateDto.builder()
                .taskId(batch.getTaskId())
                .batchId(batch.getBatchId())
                .batchStatus(BatchStatus.FOUND)
                .result(result)
                .build();
    }
}
