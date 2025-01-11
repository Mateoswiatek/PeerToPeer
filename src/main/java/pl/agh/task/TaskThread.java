package pl.agh.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.agh.task.model.Batch;
import pl.agh.task.model.enumerated.BatchStatus;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.Task;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class TaskThread implements Runnable {
    private Function<UUID, Optional<Batch>> batchProvider;
    private Consumer<BatchUpdateDto> batchUpdateMessageCallback;
    private Function<UUID, Task> taskProvider; // Function to fetch Task object by taskId

    @Getter
    private final UUID taskId;
    @Getter
    private Batch currentBatch;

    public TaskThread(Function<UUID, Optional<Batch>> batchProvider, Consumer<BatchUpdateDto> batchUpdateMessageConsumer, Function<UUID, Task> taskProvider, UUID taskId) {
        this.batchProvider = batchProvider;
        this.batchUpdateMessageCallback = batchUpdateMessageConsumer;
        this.taskProvider = taskProvider;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        Optional<Batch> optionalBatch;

        while ((optionalBatch = batchProvider.apply(taskId)).isPresent()) {
            currentBatch = optionalBatch.get();

            // Mark the batch as BOOKED
            batchUpdateMessageCallback.accept(BatchUpdateDto.getFromBatchWithStatus(currentBatch, BatchStatus.BOOKED));

            String foundResult = null;
            try {
                foundResult = processBatchWithIterator(currentBatch);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Hashing algorithm not found: " + e.getMessage());
            }

            if (foundResult != null) {
                // Fetch the task to update the result
                Task task = taskProvider.apply(taskId);
                task.complete(foundResult);

                // Mark the task as FOUND and provide the result
                batchUpdateMessageCallback.accept(BatchUpdateDto.completeTask(currentBatch, foundResult));
                break; // Exit the loop since we found the result
            }

            // Mark the batch as DONE if not FOUND
            batchUpdateMessageCallback.accept(BatchUpdateDto.getFromBatchWithStatus(currentBatch, BatchStatus.DONE));
        }
    }

    private String processBatchWithIterator(Batch batch) throws NoSuchAlgorithmException {
        BigInteger min = new BigInteger(batch.getMin());
        BigInteger max = new BigInteger(batch.getMax());
        Task task = taskProvider.apply(taskId); // Fetch the Task object
        char[] alphabet = task.getAlphabet().toCharArray(); // Convert alphabet to char array

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        PermutationIterator iterator = new PermutationIterator(min, max, task.getMaxLength().intValue(), alphabet);

        while (iterator.hasNext()) {
            String candidate = iterator.next();
            String hash = hashString(candidate, digest);

            if (hash.equals(task.getPasswordHash())) { // Compare against the password hash in Task
                return candidate; // Found the correct password
            }
        }

        return null; // No match found in this batch
    }

    private String hashString(String input, MessageDigest digest) {
        byte[] encodedhash = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public void stopTask() {
        Thread.currentThread().interrupt();
    }
}
