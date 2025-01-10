package pl.agh.mapper;

import pl.agh.task.model.Batch;
import pl.agh.task.model.dto.BatchUpdateDto;

public class BatchMapper {
    // Mapowanie z Batch na BatchUpdateDto
    public static BatchUpdateDto toBatchUpdateDto(Batch batch, String result) {
        return BatchUpdateDto.builder()
                .taskId(batch.getTaskId())
                .batchId(batch.getBatchId())
                .batchStatus(batch.getStatus())
                .result(result)
                .build();
    }

    // Mapowanie z BatchUpdateDto na Batch
    public static Batch toBatch(BatchUpdateDto batchUpdateDto) {
        return new Batch(
                batchUpdateDto.getTaskId(),
                batchUpdateDto.getBatchId(),
                null, // Brak danych dla `min`
                null, // Brak danych dla `max`
                batchUpdateDto.getBatchStatus()
        );
    }
}
