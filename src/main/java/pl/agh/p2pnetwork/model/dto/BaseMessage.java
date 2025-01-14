package pl.agh.p2pnetwork.model.dto;

import lombok.Getter;

@Getter
public abstract class BaseMessage {
    private final String type;

    protected BaseMessage(String type) {
        this.type = type;
    }
}
