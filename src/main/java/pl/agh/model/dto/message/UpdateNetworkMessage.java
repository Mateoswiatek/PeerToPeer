package pl.agh.model.dto.message;

import lombok.Builder;
import lombok.Data;
import pl.agh.model.Node;

import java.util.Set;

@Data
@Builder
public class UpdateNetworkMessage {
    Set<Node> nodes;
}
