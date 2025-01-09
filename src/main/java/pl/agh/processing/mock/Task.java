package pl.agh.processing.mock;

import java.util.Map;

public class Task {
    private final String taskId;
    private TaskStatus status;
    private final Map<String, Object> data;

    public Task(String taskId, Map<String, Object> data) {
        this.taskId = taskId;
        this.data = data;
        this.status = TaskStatus.FREE;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
