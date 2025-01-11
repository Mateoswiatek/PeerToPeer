package pl.agh.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.agh.task.model.Batch;
import pl.agh.task.model.enumerated.BatchStatus;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.Task;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class TaskThread implements Runnable {
    private Function<UUID, Optional<Batch>> batchProvider; // Pobieranie batcha
    private Consumer<BatchUpdateDto> batchUpdateMessageCallback; // Callback do aktualizacji batcha
    private Function<UUID, Task> taskProvider; // Pobieranie taska na podstawie UUID

    @Getter
    private final UUID taskId;

    @Override
    public void run() {
        Optional<Batch> optionalBatch;

        while ((optionalBatch = batchProvider.apply(taskId)).isPresent()) {
            Batch currentBatch = optionalBatch.get();

            // Oznacz batch jako BOOKED
            batchUpdateMessageCallback.accept(BatchUpdateDto.getFromBatchWithStatus(currentBatch, BatchStatus.BOOKED));

            Task task = taskProvider.apply(taskId);

            // Wywołanie strategii wykonania zadania na danym batchu
            task.execute(currentBatch);

            if (task.getResult() != null && !task.getResult().isEmpty()) {
                // Jeśli znaleziono wynik, oznacz jako FOUND
                batchUpdateMessageCallback.accept(BatchUpdateDto.completeTask(currentBatch, task.getResult()));
                break; // Kończymy przetwarzanie
            }

            // Jeśli batch się zakończył, ale nie znaleziono wyniku
            batchUpdateMessageCallback.accept(BatchUpdateDto.getFromBatchWithStatus(currentBatch, BatchStatus.DONE));
        }
    }

    public void stopTask() {
        Thread.currentThread().interrupt();
    }
}
