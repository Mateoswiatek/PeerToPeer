package pl.agh.task.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MemoryDumpDto {
    List<TaskUpdateMessageDto> taskUpdateMessageDtos;
    List<BatchUpdateDto> batchUpdateDtos;
}
