package pl.agh.task.ports.outbound;

import pl.agh.task.model.Task;

public interface DoneTaskProcessor {
    void processDoneTask(Task task);
}
