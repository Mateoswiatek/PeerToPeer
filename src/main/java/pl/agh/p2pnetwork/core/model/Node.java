package pl.agh.p2pnetwork.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;
import java.util.UUID;

@Data
public class Node {
    UUID id; // For saving in db for example and feature develop
    String ip;
    int port;

    @JsonCreator
    public Node(
            @JsonProperty("id") UUID id,
            @JsonProperty("ip") String ip,
            @JsonProperty("port") int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return port == node.port && Objects.equals(ip, node.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
