package pl.agh.task.impl;

import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.model.TaskFromNetworkMessage;
import pl.agh.p2pnetwork.NetworkManager;
import pl.agh.p2pnetwork.TCPSender;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.task.model.Task;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.ports.outbound.TaskMessageSenderPort;

import java.util.HashSet;
import java.util.Set;

public class TaskMessageSenderPortImpl implements TaskMessageSenderPort {

    private final NetworkManager networkManager;
//    private static class SingletonHolder {
//        private static final TaskMessageSenderPortImpl INSTANCE = new TaskMessageSenderPortImpl();
//    }
//
//    public static TaskMessageSenderPortImpl getInstance() {
//        return TaskMessageSenderPortImpl.SingletonHolder.INSTANCE;
//    }

    public TaskMessageSenderPortImpl(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public void sendBatchUpdateMessage(Set<Node> activeNodes, BatchUpdateDto message) {
        activeNodes.forEach( node -> networkManager.sendMessage(
                node.getIp(), node.getPort(), new BatchUpdateMessage(message)));
    }

    @Override
    public void sendTaskUpdateMessage(Set<Node> activeNodes, Task newTask) {
        activeNodes.forEach( node -> networkManager.sendMessage
                (node.getIp(), node.getPort(), new TaskFromNetworkMessage(newTask)));
    }
}
