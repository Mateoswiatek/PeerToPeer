package pl.agh.task.observer;

import pl.agh.logger.Logger;
import pl.agh.task.model.Task;
import pl.agh.task.model.TaskObserver;

public class TaskStatusLogger implements TaskObserver {
    Logger logger = Logger.getInstance();
    @Override
    public void onTaskStatusChanged(Task task) {
        logger.info("Task status changed! Task ID: " + task.getTaskId() +
                ", New status: " + task.getTaskStatus());
        if (task.getResult() != null && !task.getResult().isEmpty()) {
            logger.info("Task result: " + task.getResult());
        }
    }
}
