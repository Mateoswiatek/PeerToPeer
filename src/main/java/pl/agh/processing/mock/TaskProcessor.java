package pl.agh.processing.mock;

public class TaskProcessor {
    private TaskExecutionStrategy strategy;

    public TaskProcessor(TaskExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(TaskExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    public void process(Task task) {
        System.out.println("Processing task: " + task.getTaskId());
        strategy.execute(task);
        task.setStatus(TaskStatus.COMPLETED);
    }
}
