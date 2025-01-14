package pl.agh.middleware.mapper;

import pl.agh.middleware.p2p.model.task.NewTaskRequest;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessageRequestMessage;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.task.model.dto.NewTaskDto;
import pl.agh.task.model.dto.TaskUpdateMessageDto;
import pl.agh.task.model.dto.TaskUpdateMessageRequestDto;
import pl.agh.task.model.enumerated.TaskStatus;

public class TaskMapper {

    public static TaskUpdateMessage toTaskUpdateMessage(TaskUpdateMessageDto task) {
        return new TaskUpdateMessage(
                task.getTaskId(),
                task.getPasswordHash(),
                task.getAlphabet(),
                task.getMaxLength(),
                task.getMaxBatchSize(),
                task.getTaskStatus(),
                task.getResult());
    }

    public static TaskUpdateMessageDto toTaskUpdateMessageDto(TaskUpdateMessage task) {
        return new TaskUpdateMessageDto(
                task.getTaskId(),
                task.getPasswordHash(),
                task.getAlphabet(),
                task.getMaxLength(),
                task.getMaxBatchSize(),
                task.getTaskStatus(),
                task.getResult());
    }

    public static NewTaskDto toNewTaskDto(NewTaskRequest taskRequest) {
        return new NewTaskDto(taskRequest.getPasswordHash(),
                taskRequest.getAlphabet(),
                taskRequest.getMaxLength(),
                taskRequest.getMaxBatchSize(),
                TaskStatus.CREATED);
    }

    public static TaskUpdateMessageRequestMessage toTaskUpdateMessageRequestMessage(TaskUpdateMessageRequestDto taskUpdateMessageRequestDto, Node node) {
        return new TaskUpdateMessageRequestMessage(taskUpdateMessageRequestDto.getTaskId(), node);
    }
}
