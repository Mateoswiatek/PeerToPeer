package pl.agh.middleware.mapper;

import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.task.model.Batch;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.enumerated.BatchStatus;

public class BatchMapper {
    public static BatchUpdateDto toBatchUpdateDto(BatchUpdateMessage batch) {
        return new BatchUpdateDto(batch.getTaskId(),
                batch.getBatchId(),
                batch.getBatchStatus(),
                batch.getResult());
    }


    public static BatchUpdateDto getFromBatchWithStatus(Batch batch, BatchStatus newStatus) {
        return new BatchUpdateDto(batch.getTaskId(),
                batch.getBatchId(),
                newStatus,
                null);
    }

    public static BatchUpdateMessage toTaskUpdateMessage(BatchUpdateDto batch) {
        return new BatchUpdateMessage(batch.getTaskId(),
                batch.getBatchId(),
                batch.getBatchStatus(),
                batch.getResult());
    }

}
