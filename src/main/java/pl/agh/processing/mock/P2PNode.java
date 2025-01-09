package pl.agh.processing.mock;

public class P2PNode implements TaskCompletionObserver {
    private final String nodeId;

    public P2PNode(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void onTaskCompleted(Task task) {
        System.out.println("Node " + nodeId + " syncing completed task: " + task.getTaskId());
        // Sync logic here
    }
}