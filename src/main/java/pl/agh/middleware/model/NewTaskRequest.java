package pl.agh.middleware.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NewTaskRequest extends BaseMessage {
    private final String passwordHash;
    private final String alphabet;
    private final Long maxLength;
    private final Long maxBatchSize;

    @JsonCreator
    public NewTaskRequest(
            @JsonProperty("passwordHash") String passwordHash,
            @JsonProperty("alphabet") String alphabet,
            @JsonProperty("maxLength") Long maxLength,
            @JsonProperty("maxBatchSize") Long maxBatchSize) {
        super(NewTaskRequest.class.getSimpleName());
        this.passwordHash = passwordHash;
        this.alphabet = alphabet;
        this.maxLength = maxLength;
        this.maxBatchSize = maxBatchSize;
    }

}
