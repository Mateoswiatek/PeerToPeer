package pl.agh.kernel;

import pl.agh.model.Batch;
import pl.agh.model.enumerated.BathStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchRepository {
    void save(Batch batch);
    void saveAll(List<Batch> batches);
    List<Batch> findAll();
    Optional<Batch> findById(UUID id);
    List<Batch> findByStatus(BathStatus status);
    List<Batch> findByTaskId(UUID taskId);
    void updateStatus(UUID id, BathStatus status);

//    void update(Batch batch);
//    void deleteById(int id);
//    void deleteAll();
}
