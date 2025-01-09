package pl.agh.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.agh.model.Node;
import pl.agh.model.dto.BaseMessage;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class JoinToNetworkRequest extends BaseMessage {
    Node newNode;

    @JsonCreator
    public JoinToNetworkRequest(@JsonProperty("newNode") Node newNode) {
        super(JoinToNetworkRequest.class.getSimpleName());
        this.newNode = newNode;
    }
}
