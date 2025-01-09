package pl.agh.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.agh.model.Batch;

import java.util.List;

public record BatchTable(List<Batch> batches) {
    @JsonCreator
    public BatchTable(@JsonProperty("batches") List<Batch> batches) {
        this.batches = batches;
    }
}