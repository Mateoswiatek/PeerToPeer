package pl.agh.task.impl;

import pl.agh.task.model.Task;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.ports.outbound.TaskMessageSenderPort;

public class TaskMessageSenderPortImpl implements TaskMessageSenderPort {

    private static class SingletonHolder {
        private static final TaskMessageSenderPortImpl INSTANCE = new TaskMessageSenderPortImpl();
    }

    public static TaskMessageSenderPortImpl getInstance() {
        return TaskMessageSenderPortImpl.SingletonHolder.INSTANCE;
    }

    @Override
    public void sendBatchUpdateMessage(BatchUpdateDto message) {

    }

    @Override
    public void sendTaskUpdateMessage(Task newTask) {

    }
}
