package pl.agh.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.agh.model.enumerated.BathStatus;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Batch {
    private UUID taskId;
    private UUID id;
    private String min;
    private String max;
    private BathStatus status;

    @JsonCreator
    public Batch(
            @JsonProperty("taskId") UUID taskId,
            @JsonProperty("id") UUID id,
            @JsonProperty("min") String min,
            @JsonProperty("max") String max,
            @JsonProperty("status") BathStatus status) {
        this.taskId = taskId;
        this.id = id;
        this.min = min;
        this.max = max;
        this.status = status;
    }
}