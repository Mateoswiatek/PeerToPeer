package pl.agh.task.ports.inbound;

import java.util.UUID;

public interface TaskService<TID, TRequest> {
    void createTask(TID taskId, TRequest taskRequest);
    void startTask(TID taskId);
}
