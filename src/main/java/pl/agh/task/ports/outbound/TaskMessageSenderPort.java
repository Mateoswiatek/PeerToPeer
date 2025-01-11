package pl.agh.task.ports.outbound;

import pl.agh.p2pnetwork.model.Node;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.Task;

import java.util.Set;

//TODO (10.01.2025): Zrobić implementację tego, tak aby się wysyłało na TCP oraz przyjmowanie tego, w dopiwedni sposób
public interface TaskMessageSenderPort {
    void sendBatchUpdateMessage(Set<Node> activeNodes, BatchUpdateDto message);
    void sendTaskUpdateMessage(Set<Node> activeNodes, Task newTask);
}
