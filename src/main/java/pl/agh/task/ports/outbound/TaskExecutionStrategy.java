package pl.agh.task.ports.outbound;

import pl.agh.task.model.Batch;
import pl.agh.task.model.Task;

public interface TaskExecutionStrategy {
    void execute(Task task, Batch batch);

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
