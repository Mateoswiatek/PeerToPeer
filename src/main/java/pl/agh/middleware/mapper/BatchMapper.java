package pl.agh.middleware.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.task.model.dto.BatchUpdateDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchMapper {
    public static BatchUpdateDto toBatchUpdateDto(BatchUpdateMessage batch) {
        return new BatchUpdateDto(batch.getTaskId(),
                batch.getBatchId(),
                batch.getBatchStatus(),
                batch.getResult());
    }

    public static BatchUpdateMessage toTaskUpdateMessage(BatchUpdateDto batch) {
        return new BatchUpdateMessage(batch.getTaskId(),
                batch.getBatchId(),
                batch.getBatchStatus(),
                batch.getResult());
    }

}
