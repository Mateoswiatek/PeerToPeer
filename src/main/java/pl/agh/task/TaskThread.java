package pl.agh.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.agh.logger.Logger;
import pl.agh.task.mapper.BatchMapper;
import pl.agh.task.model.Batch;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.enumerated.BatchStatus;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class TaskThread implements Runnable {
    private Function<UUID, Optional<Batch>> batchProvider; // Pobieranie batcha
    private Consumer<BatchUpdateDto> batchUpdateMessageCallback; // Callback do aktualizacji batcha
    private Function<UUID, Task> taskProvider; // Pobieranie taska na podstawie UUID
    private final Logger logger = Logger.getInstance();

    @Getter
    private final UUID taskId;

    @Getter
    private Batch currentBatch;

    @Override
    public void run() {
        logger.info("Start task thread");
        Optional<Batch> optionalBatch;

        while ((optionalBatch = batchProvider.apply(taskId)).isPresent()) {
            currentBatch = optionalBatch.get();

            try {
                // Oznacz batch jako BOOKED
                batchUpdateMessageCallback.accept(BatchMapper.getFromBatchWithStatus(currentBatch, BatchStatus.BOOKED));
            } catch (Exception e) {
                logger.error("Error when trying to send booking message: " + e);
            }

            Task task = taskProvider.apply(taskId);
            // Wywołanie strategii wykonania zadania na danym batchu
            task.execute(currentBatch);

            if (task.getResult() != null && !task.getResult().isEmpty()) {
                logger.info("Batch finished - FOUND RESULT, send result info to network, result: " + task.getResult());
                batchUpdateMessageCallback.accept(BatchUpdateDto.completeTask(currentBatch, task.getResult()));
                break; // Kończymy przetwarzanie
            }

            // Jeśli batch się zakończył, ale nie znaleziono wyniku
            logger.info("Batch finished, inform other network members. Task: " + currentBatch.getTaskId() + " Batch: " + currentBatch.getBatchId());
            batchUpdateMessageCallback.accept(BatchMapper.getFromBatchWithStatus(currentBatch, BatchStatus.DONE));
        }
        logger.info("Task Thread finished");
    }

    public void stopTask() {
        Thread.currentThread().interrupt();
    }
}
