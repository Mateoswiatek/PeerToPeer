package pl.agh.task.observer;

import pl.agh.task.model.Task;
import pl.agh.task.model.TaskObserver;

public class TaskStatusLogger implements TaskObserver {
    @Override
    public void onTaskStatusChanged(Task task) {
        System.out.println("Task status changed! Task ID: " + task.getTaskId() +
                ", New status: " + task.getTaskStatus());
        if (task.getResult() != null && !task.getResult().isEmpty()) {
            System.out.println("Task result: " + task.getResult());
        }
    }
}
