package pl.agh.task.ports.outbound;

import pl.agh.task.model.Batch;
import pl.agh.task.model.dto.BatchUpdateDto;
import pl.agh.task.model.enumerated.BatchStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchRepositoryPort {
    void save(Batch batch);
    void saveAll(List<Batch> batches);
    List<Batch> findAll();
    Optional<Batch> findByTaskIdAndBatchId(UUID taskId, Long id);
    List<Batch> findAllByStatusAndTaskId(BatchStatus status, UUID taskId);
    List<Batch> findAllByTaskId(UUID taskId);
    void updateStatus(UUID taskId, Long id, BatchStatus status);
    void deleteByTaskId(UUID taskId);
    void updateStatusFromDump(List<BatchUpdateDto> batchUpdateDtos);

//    void update(Batch batch);
//    void deleteById(int id);
//    void deleteAll();
}
