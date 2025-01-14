package pl.agh.middleware.task;

import pl.agh.task.model.Task;

public interface DoneTaskProcessor {
    void processDoneTask(Task task);
}
