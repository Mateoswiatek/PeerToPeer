package pl.agh.task.model;

public interface TaskObserver {
    void onTaskStatusChanged(Task task);
}
