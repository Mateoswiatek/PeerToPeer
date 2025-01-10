package pl.agh.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.agh.task.model.Batch;
import pl.agh.task.model.enumerated.BatchStatus;
import pl.agh.task.model.dto.BatchUpdateDto;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class TaskThread implements Runnable {
    private Function<UUID, Optional<Batch>> batchProvider;
    private Consumer<BatchUpdateDto> batchUpdateMessageCallback;

    @Getter
    private final UUID taskId;
    @Getter
    private Batch currentBatch;

    public TaskThread(Function<UUID, Optional<Batch>> batchProvider, Consumer<BatchUpdateDto> batchUpdateMessageConsumer, UUID taskId) {
        this.batchProvider = batchProvider;
        this.batchUpdateMessageCallback = batchUpdateMessageConsumer;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        Optional<Batch> optionalBatch;

        while ((optionalBatch = batchProvider.apply(taskId)).isPresent()) {
            currentBatch = optionalBatch.get();
            batchUpdateMessageCallback.accept(BatchUpdateDto.getFromBatchWithStatus(currentBatch, BatchStatus.BOOKED));

            //TODO (10.01.2025): Dodać to hashowanie dla batcha, sprawdzanie
            // Jeśli uda się znaleźć, to ustawiamy na FOUND.
            batchUpdateMessageCallback.accept(BatchUpdateDto.completeTask(currentBatch, "WYNIK"));

            batchUpdateMessageCallback.accept(BatchUpdateDto.getFromBatchWithStatus(currentBatch, BatchStatus.DONE));
        }
    }

    public void stopTask() {
        Thread.currentThread().interrupt();
    }
}
