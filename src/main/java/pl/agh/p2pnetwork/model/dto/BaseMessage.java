package pl.agh.p2pnetwork.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import pl.agh.p2pnetwork.model.Node;

@Getter
public abstract class BaseMessage {
    private final String type;
    private Node node;

    protected BaseMessage(String type) {
        this.type = type;
    }

    @JsonProperty("node")
    public Node getNode() {
        return node;
    }

    @JsonProperty("node")
    public void setNode(Node node) {
        this.node = node;
    }
}
