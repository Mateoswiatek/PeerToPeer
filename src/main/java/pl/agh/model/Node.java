package pl.agh.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Node {
    UUID id;
    String ip;
    int port;
}
