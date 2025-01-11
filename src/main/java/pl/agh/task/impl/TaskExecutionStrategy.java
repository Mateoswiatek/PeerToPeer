package pl.agh.task.impl;

import pl.agh.task.model.Batch;
import pl.agh.task.model.Task;

public interface TaskExecutionStrategy {
    void execute(Task task, Batch batch);
}
