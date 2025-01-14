package pl.agh.p2pnetwork.model.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

public class Ping extends BaseMessage {
    private final String ping;

    @JsonCreator
    public Ping(@JsonProperty("ping") String ping) {
        super(Ping.class.getSimpleName());
        this.ping = ping;
    }

    public static Ping ping() {
        return new Ping("ping");
    }
}
