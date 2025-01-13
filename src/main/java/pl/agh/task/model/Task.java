package pl.agh.task.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.agh.task.impl.TaskExecutionStrategy;
import pl.agh.task.model.enumerated.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Task {
    private UUID taskId;
    private String passwordHash;
    private String alphabet;
    private Long maxLength;
    private Long maxBatchSize;
    private TaskStatus taskStatus;
    private String result = "";
    private TaskExecutionStrategy strategy;

    private final List<TaskObserver> observers = new ArrayList<>(); // Lista obserwatorów

    public void complete(String result) {
        this.result = result;
        this.taskStatus = TaskStatus.DONE;
        notifyObservers(); // Powiadom obserwatorów o zmianie
    }

    public void execute(Batch batch) {
        if (strategy != null) {
            strategy.execute(this, batch); // Wywołanie strategii z batch
        } else {
            throw new IllegalStateException("Brak strategii wykonania dla zadania.");
        }
    }

    public void addObserver(TaskObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TaskObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (TaskObserver observer : observers) {
            observer.onTaskStatusChanged(this);
        }
    }
}
