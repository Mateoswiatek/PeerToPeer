package pl.agh.processing.mock;

public class BruteForceTaskExecutionStrategy implements TaskExecutionStrategy {
    @Override
    public void execute(Task task) {
        System.out.println("Executing task with brute-force strategy: " + task.getTaskId());
        // Add brute-force logic here.
    }
}
