package pl.agh.task.ports.outbound;

import pl.agh.task.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Można by się bawić w dziedziczenie po tasku, aby do bazki leciało wszystki jak w springu typowe repo, ale
 * to niech ktoś inny się tym bawi i to robi
 */
public interface TaskRepositoryPort {
    Task save(Task task);
    Optional<Task> getById(UUID taskId);
    List<Task> findAll();
}
