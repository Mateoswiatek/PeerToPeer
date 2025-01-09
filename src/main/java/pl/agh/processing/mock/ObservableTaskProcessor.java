package pl.agh.processing.mock;

import java.util.ArrayList;
import java.util.List;

public class ObservableTaskProcessor extends TaskProcessor {
    private final List<TaskCompletionObserver> observers = new ArrayList<>();

    public ObservableTaskProcessor(TaskExecutionStrategy strategy) {
        super(strategy);
    }

    public void addObserver(TaskCompletionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TaskCompletionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void process(Task task) {
        super.process(task); // Process task with the strategy
        notifyObservers(task);
    }

    private void notifyObservers(Task task) {
        for (TaskCompletionObserver observer : observers) {
            observer.onTaskCompleted(task);
        }
    }
}

