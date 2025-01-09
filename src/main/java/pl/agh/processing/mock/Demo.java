package pl.agh.processing.mock;

import java.util.HashMap;


public class Demo {
    public static void main(String[] args) {
        // Create Task Factory
        TaskFactory factory = new TaskFactory();

        // Create Tasks
        Task task1 = factory.createTask("Task1", new HashMap<>());
        Task task2 = factory.createTask("Task2", new HashMap<>());

        // Create TaskProcessor with BruteForce strategy
        TaskExecutionStrategy bruteForceStrategy = new BruteForceTaskExecutionStrategy();
        ObservableTaskProcessor taskProcessor = new ObservableTaskProcessor(bruteForceStrategy);

        // Add observers
        taskProcessor.addObserver(new P2PNode("Node1"));
        taskProcessor.addObserver(new P2PNode("Node2"));

        // Process tasks
        taskProcessor.process(task1);
        taskProcessor.process(task2);

        // Mark tasks as completed in factory
        factory.markTaskAsCompleted(task1.getTaskId());
        factory.markTaskAsCompleted(task2.getTaskId());

        // Change strategy to Distributed and process a new task
        TaskExecutionStrategy distributedStrategy = new BruteForceTaskExecutionStrategy();
        taskProcessor.setStrategy(distributedStrategy);

        Task task3 = factory.createTask("Task3", new HashMap<>());
        taskProcessor.process(task3);

        factory.markTaskAsCompleted(task3.getTaskId());
    }
}