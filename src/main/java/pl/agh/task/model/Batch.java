package pl.agh.task.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.agh.task.model.enumerated.BatchStatus;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Batch {
//    Klucz główny to TaskId wraz z batchId
    private UUID taskId;
    private Long batchId;
    private String min;
    private String max;
    private BatchStatus status;

    public Batch(
            UUID taskId,
            Long batchId,
            String min,
            String max,
            BatchStatus status) {
        this.taskId = taskId;
        this.batchId = batchId;
        this.min = min;
        this.max = max;
        this.status = status;
    }
}