package pl.agh.p2pnetwork.model.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

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
