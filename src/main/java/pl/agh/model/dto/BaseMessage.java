package pl.agh.model.dto;

public abstract class BaseMessage {
    private String type;

    public BaseMessage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
