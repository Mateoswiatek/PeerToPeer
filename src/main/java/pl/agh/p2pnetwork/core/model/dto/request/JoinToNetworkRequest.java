package pl.agh.p2pnetwork.core.model.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.p2pnetwork.core.model.Node;

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
