package pl.agh.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.agh.model.Node;
import pl.agh.model.dto.message.UpdateNetworkMessage;
import pl.agh.model.dto.request.JoinToNetworkRequest;

import java.util.HashSet;
import java.util.Set;

public class NetworkManager {
    Node myself;
    Set<Node> nodes = new HashSet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NetworkManager(Node myself) {
        this.myself = myself;
    }

    public void connectMyselfToNetwork(String ip, Integer port) {
        System.out.println("connect to Network invoked. ip: " + ip + ", port: " + port);

        JoinToNetworkRequest joinToNetworkRequest = new JoinToNetworkRequest(myself);
        //TODO (09.01.2025): Wysłanie requesta do określonego noda.
        // Łączymy się na ip:port
    }

    public void addNewNodeToNetwork(JoinToNetworkRequest joinToNetworkRequest) {
        nodes.add(joinToNetworkRequest.newNode());

        publishMessage(UpdateNetworkMessage.builder()
                .nodes(nodes)
                .build());
    }

    private void publishMessage(Object object) {
        try {
            String message = objectMapper.writeValueAsString(object);
            nodes.forEach(node -> {
                node.getIp();
                node.getPort();
                // Wysyłamy message jako Stringa (JSON)
                //TODO (09.01.2025): Wysyłanie wiadomości jako String na określony adres
                // Wysyłamy tutaj naszego message do wszystkich elementów w secie.
                // Ewentualnie dodać w parametrach tej metody nw, jakiś endpoint czy coś, jeśli będzie trzeba rozróżniać różne komendy.
                // Ewentualnie każdy message będzie mieć pole w sylu "Type", gdzie przekazujemy nazwę klasy modelu i w listenerze
                // robimy mappera na podstawie nazwy tej klasy
            });
        } catch (JsonProcessingException e ) {
            e.printStackTrace();
        }
    }






}
