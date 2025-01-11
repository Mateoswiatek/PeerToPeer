package pl.agh.task.model;

import lombok.Builder;
import lombok.Getter;
import pl.agh.task.impl.TaskExecutionStrategy;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.enumerated.TaskStatus;

import java.util.UUID;

@Getter
@Builder
public class Task {
    private UUID taskId;
    private String passwordHash;
    private String alphabet;
    private Long maxLength;
    private Long maxBatchSize;
    private TaskStatus taskStatus;
    private String result = "";
    private TaskExecutionStrategy strategy;

    public static Task fromNewTaskRequest(NewTaskDto newTaskRequest, TaskStatus taskStatus, TaskExecutionStrategy strategy) {
        return Task.builder()
                .passwordHash(newTaskRequest.getPasswordHash())
                .alphabet(newTaskRequest.getAlphabet())
                .maxLength(newTaskRequest.getMaxLength())
                .maxBatchSize(newTaskRequest.getMaxBatchSize())
                .taskStatus(taskStatus)
                .strategy(strategy)
                .build();
    }

    public void complete(String result) {
        this.result = result;
        this.taskStatus = TaskStatus.DONE;
    }

    public void execute(Batch batch) {
        if (strategy != null) {
            strategy.execute(this, batch); // Wywołanie strategii z batch
        } else {
            throw new IllegalStateException("Brak strategii wykonania dla zadania.");
        }
    }
}
