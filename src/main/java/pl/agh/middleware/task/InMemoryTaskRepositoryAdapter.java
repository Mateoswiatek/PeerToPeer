package pl.agh.middleware.task;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.task.model.Task;
import pl.agh.task.model.enumerated.TaskStatus;
import pl.agh.task.ports.outbound.TaskRepositoryPort;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InMemoryTaskRepositoryAdapter implements TaskRepositoryPort {
    private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();
    private final Logger logger = Logger.getInstance();

    private static class SingletonHolder {
        private static final InMemoryTaskRepositoryAdapter INSTANCE = new InMemoryTaskRepositoryAdapter();
    }

    public static InMemoryTaskRepositoryAdapter getInstance() {
        return InMemoryTaskRepositoryAdapter.SingletonHolder.INSTANCE;
    }

    @Override
    public Task save(Task task) {
//        logger.info("Save task to repository.");

        if(task.getTaskId() == null) {
            task.setTaskId(UUID.randomUUID());
            logger.info("Task has no id, new randomUUID: " + task.getTaskId());
        }

        // Nie ważne, czy został stworzony u nas, czy w innym node, zapisujemy.
        tasks.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Optional<Task> getById(UUID taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public List<Task> findAll() {
        return tasks.values().stream().toList();
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        return tasks.values().stream().filter(task -> status.equals(task.getTaskStatus())).toList();
    }
}