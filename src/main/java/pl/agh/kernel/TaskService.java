package pl.agh.kernel;

import lombok.AllArgsConstructor;
import pl.agh.model.Batch;
import pl.agh.model.dto.request.NewTaskRequest;
import pl.agh.model.enumerated.BathStatus;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class TaskService {
    private final BatchRepository batchRepository;

//    Dodanie abstrakcyjnej klaski, interface, która ma metodki do wysyłania updatów na temat tasku oraz pobiera info od innych.

//    @Async
    public void createTask(UUID taskId, NewTaskRequest request) {
        BigInteger totalPermutations = calculateTotalPermutations(request.getAlphabet().length(), request.getMaxLength());

        List<Batch> batches = new ArrayList<>();
        BigInteger currentMin = BigInteger.ZERO;
        BigInteger maxBatchSize = BigInteger.valueOf(request.getMaxBatchSize());

        while (currentMin.compareTo(totalPermutations) < 0) {
            BigInteger currentMax = currentMin.add(maxBatchSize).subtract(BigInteger.ONE);
            if (currentMax.compareTo(totalPermutations) >= 0) {
                currentMax = totalPermutations.subtract(BigInteger.ONE);
            }

            Batch batch = new Batch(
                    taskId,
                    UUID.randomUUID(),
                    currentMin.toString(),
                    currentMax.toString(),
                    BathStatus.NOT_DONE
            );

            batches.add(batch);
            currentMin = currentMax.add(BigInteger.ONE);
        }

        System.out.println("Utworzono partie: " + batches.size());
        //TODO (10.01.2025): Zapis do bazy + wysłanie informacji w celu synchronizacji
    }

    public BigInteger calculateTotalPermutations(int alphabetLength, Long maxLength) {
        BigInteger total = BigInteger.ZERO;
        BigInteger currentPower = BigInteger.ONE;
        BigInteger alphabetSize = BigInteger.valueOf(alphabetLength);

        for (int i = 1; i <= maxLength; i++) {
            currentPower = currentPower.multiply(alphabetSize);
            total = total.add(currentPower);
        }

        return total;
    }
}
