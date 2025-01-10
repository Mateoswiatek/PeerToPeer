package pl.agh.task.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.task.ports.outbound.BatchRepositoryPort;
import pl.agh.task.model.Batch;
import pl.agh.task.model.enumerated.BatchStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InMemoryBatchRepositoryAdapter implements BatchRepositoryPort {
    private final Map<UUID, Map<Long, Batch>> taskIdIndex = new ConcurrentHashMap<>();

    // Klasa wewnętrzna, która trzyma jedyną instancję Singletona
    private static class SingletonHolder {
        private static final InMemoryBatchRepositoryAdapter INSTANCE = new InMemoryBatchRepositoryAdapter();
    }

    public static InMemoryBatchRepositoryAdapter getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public void save(Batch batch) {
        taskIdIndex.computeIfAbsent(batch.getTaskId(), k -> new ConcurrentHashMap<>())
                .put(batch.getBatchId(), batch);
    }

    @Override
    public void saveAll(List<Batch> batches) {
        // Grupowanie batchy po taskId
        Map<UUID, List<Batch>> groupedBatches = batches.stream()
                .collect(Collectors.groupingBy(Batch::getTaskId));

        // Iteracja po pogrupowanych batchach i dodawanie ich do taskIdIndex
        groupedBatches.forEach((taskId, batchList) -> {
            taskIdIndex.computeIfAbsent(taskId, k -> new ConcurrentHashMap<>());
            batchList.forEach(batch -> taskIdIndex.get(taskId).put(batch.getBatchId(), batch));
        });
    }

    @Override
    public List<Batch> findAll() {
        return taskIdIndex.values().stream()
                .flatMap(taskBatches -> taskBatches.values().stream())
                .toList();
    }

    @Override
    public Optional<Batch> findByTaskIdAndBatchId(UUID taskId, Long id) {
        return Optional.ofNullable(taskIdIndex.getOrDefault(taskId, Collections.emptyMap()).get(id));
    }

    @Override
    public List<Batch> findAllByStatusAndTaskId(BatchStatus status, UUID taskId) {
        return taskIdIndex.getOrDefault(taskId, Collections.emptyMap()).values().stream()
                .filter(batch -> batch.getStatus().equals(status)).toList();
    }

    @Override
    public List<Batch> findAllByTaskId(UUID taskId) {
        return new ArrayList<>(taskIdIndex.getOrDefault(taskId, Collections.emptyMap()).values());
    }

    @Override
    public void updateStatus(UUID taskId, Long id, BatchStatus status) {
        findByTaskIdAndBatchId(taskId, id).ifPresentOrElse(
                batch -> batch.setStatus(status),
                () -> { throw new RuntimeException("Batch not found for id: " + id); }
        );
    }

//    @Override
//    public void update(Batch batch) {
//        storage.put(batch.id(), batch);
//    }
//
//    @Override
//    public void deleteById(int id) {
//        storage.remove(id);
//    }
//
//    @Override
//    public void deleteAll() {
//        storage.clear();
//    }
}