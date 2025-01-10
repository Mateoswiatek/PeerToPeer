package pl.agh.p2pnetwork.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class Node {
    UUID id;
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
}
