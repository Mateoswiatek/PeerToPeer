package pl.agh.task.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.logger.Logger;
import pl.agh.task.model.Task;
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
        logger.info("Save task to repository...");
        if (task.getTaskId() != null && !tasks.containsKey(task.getTaskId())) {
            logger.info("Task has ID, save it to MAP" + task.getTaskId());
            tasks.put(task.getTaskId(), task);
            return task;
        } else if (task.getTaskId() == null) {
            UUID taskId = UUID.randomUUID();

            Task newTask = new Task(
                    taskId,
                    task.getPasswordHash(),
                    task.getAlphabet(),
                    task.getMaxLength(),
                    task.getMaxBatchSize(),
                    task.getTaskStatus(),
                    task.getResult(),
                    task.getStrategy()
            );

            logger.info("Task has no ID, put task with random ID: " + taskId);
            tasks.put(taskId, newTask);
            return newTask;
        }
        else {
            logger.error("Task sent with ID that is already present");
            return null;
        }
    }

    @Override
    public Optional<Task> getById(UUID taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public List<Task> findAll() {
        return tasks.values().stream().toList();
    }
}