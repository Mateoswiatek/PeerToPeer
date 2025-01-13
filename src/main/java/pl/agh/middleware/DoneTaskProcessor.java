package pl.agh.middleware;

import pl.agh.task.model.Task;

public interface DoneTaskProcessor {
    void processDoneTask(Task task);
}
