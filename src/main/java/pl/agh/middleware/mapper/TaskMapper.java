package pl.agh.middleware.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.middleware.model.BatchUpdateMessage;
import pl.agh.middleware.model.MemoryDumpMessage;
import pl.agh.middleware.p2p.model.task.NewTaskRequest;
import pl.agh.middleware.p2p.model.task.TaskDumpMessageRequestMessage;
import pl.agh.middleware.p2p.model.task.TaskUpdateMessage;
import pl.agh.task.model.dto.*;
import pl.agh.task.model.enumerated.TaskStatus;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static TaskDumpMessageRequestMessage toTaskUpdateMessageRequestMessage(TaskUpdateMessageRequestDto taskUpdateMessageRequestDto) {
        return new TaskDumpMessageRequestMessage(taskUpdateMessageRequestDto.getTaskId());
    }

    public static MemoryDumpMessage toMemoryDumpMessage(MemoryDumpDto memoryDumpDto) {
        List<TaskUpdateMessage> taskUpdateMessageList =  memoryDumpDto.getTaskUpdateMessageDtos().stream().map(TaskMapper::toTaskUpdateMessage).toList();
        List<BatchUpdateMessage> batchUpdateMessageList = memoryDumpDto.getBatchUpdateDtos().stream().map(BatchMapper::toTaskUpdateMessage).toList();
        return new MemoryDumpMessage(taskUpdateMessageList, batchUpdateMessageList);
    }

    public static MemoryDumpDto toMemoryDumpDto(MemoryDumpMessage memoryDumpMessage) {
        List<TaskUpdateMessageDto> taskUpdateMessageList =  memoryDumpMessage.getTaskUpdateMessages().stream().map(TaskMapper::toTaskUpdateMessageDto).toList();
        List<BatchUpdateDto> batchUpdateMessageList = memoryDumpMessage.getBatchUpdateMessages().stream().map(BatchMapper::toBatchUpdateDto).toList();
        return new MemoryDumpDto(taskUpdateMessageList, batchUpdateMessageList);
    }
}
