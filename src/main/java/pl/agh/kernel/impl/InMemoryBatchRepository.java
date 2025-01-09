package pl.agh.kernel.impl;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pl.agh.kernel.BatchRepository;
import pl.agh.model.Batch;
import pl.agh.model.enumerated.BathStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InMemoryBatchRepository implements BatchRepository {
    private final Map<UUID, Batch> storage = new ConcurrentHashMap<>();

    // Klasa wewnętrzna, która trzyma jedyną instancję Singletona
    private static class SingletonHolder {
        private static final InMemoryBatchRepository INSTANCE = new InMemoryBatchRepository();
    }

    // Publiczna metoda dostępu do instancji
    public static InMemoryBatchRepository getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public void save(Batch batch) {
        storage.put(batch.getId(), batch);
    }

    @Override
    public void saveAll(List<Batch> batches) {
        for (Batch batch : batches) {
            storage.put(batch.getId(), batch);
        }
    }

    @Override
    public List<Batch> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Batch> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Batch> findByStatus(BathStatus status) {
        return storage.values().stream().filter(batch -> batch.getStatus().equals(status)).collect(Collectors.toList());
    }

    @Override
    public List<Batch> findByTaskId(UUID taskId) {
        return storage.values().stream().filter(batch -> batch.getTaskId().equals(taskId)).collect(Collectors.toList());
    }

    @Override
    public void updateStatus(UUID id, BathStatus status) {
        this.findById(id).ifPresentOrElse(batch -> batch.setStatus(status), () -> {throw new RuntimeException("NOT_FOUND");});
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