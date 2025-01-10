package pl.agh.middleware.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pl.agh.task.model.dto.BatchUpdateDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class MemoryDumpMessage {
    List<TaskFromNetworkMessage> tasksFromNetworkMessages;
    List<BatchUpdateDto> batchUpdateDtoList;
}
