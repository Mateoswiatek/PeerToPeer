package pl.agh.task.mapper;

import pl.agh.task.model.Batch;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.enumerated.BatchStatus;

public class BatchMapper {
    public static BatchUpdateDto toBatchUpdateDto(Batch batch) {
        return new BatchUpdateDto(batch.getTaskId(),
                batch.getBatchId(),
                batch.getStatus(),
                null);
    }

    public static BatchUpdateDto getFromBatchWithStatus(Batch batch, BatchStatus newStatus) {
        return new BatchUpdateDto(batch.getTaskId(),
                batch.getBatchId(),
                newStatus,
                null);
    }
}
