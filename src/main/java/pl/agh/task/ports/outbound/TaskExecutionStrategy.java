package pl.agh.task.ports.outbound;

import pl.agh.task.model.Batch;
import pl.agh.task.model.Task;
//TODO (08.02.2025): It isn't very well example of Strategy design pattern.
public interface TaskExecutionStrategy {
    void execute(Task task, Batch batch);

    default String getName() {
        return this.getClass().getSimpleName();
    }
}
