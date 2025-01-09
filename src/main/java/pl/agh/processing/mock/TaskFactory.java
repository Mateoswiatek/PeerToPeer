package pl.agh.processing.mock;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TaskFactory {
    private final Set<String> completedTasks = new HashSet<>();

    public Task createTask(String taskId, Map<String, Object> data) {
        if (completedTasks.contains(taskId)) {
            throw new IllegalArgumentException("Task already completed: " + taskId);
        }
        return new Task(taskId, data);
    }

    public void markTaskAsCompleted(String taskId) {
        completedTasks.add(taskId);
    }
}
