package pl.agh.p2pnetwork.core.model.dto.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.agh.p2pnetwork.core.model.dto.BaseMessage;
import pl.agh.p2pnetwork.core.model.Node;

import java.util.Set;

@Getter
@Setter
@Builder
public class UpdateNetworkMessage extends BaseMessage {
    Set<Node> nodes;

    @JsonCreator
    public UpdateNetworkMessage(@JsonProperty("nodes") Set<Node> nodes) {
        super(UpdateNetworkMessage.class.getSimpleName());
        this.nodes = nodes;
    }
}
