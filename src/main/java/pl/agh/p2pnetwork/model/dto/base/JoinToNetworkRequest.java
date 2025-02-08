package pl.agh.p2pnetwork.model.dto.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class JoinToNetworkRequest extends BaseMessage {
    //TODO (08.02.2025): tak naprawdę ten newNode nie jest potrzebny, bo i tak wysyłamy Node w bazowym.
    Node newNode;

    @JsonCreator
    public JoinToNetworkRequest(@JsonProperty("newNode") Node newNode) {
        super(JoinToNetworkRequest.class.getSimpleName());
        this.newNode = newNode;
    }
}
