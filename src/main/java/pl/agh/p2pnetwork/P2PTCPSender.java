package pl.agh.p2pnetwork;

import pl.agh.p2pnetwork.base.P2PMessageResolver;
import pl.agh.p2pnetwork.model.Node;
import pl.agh.p2pnetwork.model.dto.BaseMessage;

import java.io.IOException;

public abstract class P2PTCPSender {
    P2PMessageResolver messageResolver;


    protected void sendMessageToNode(Node node, BaseMessage baseMessage) throws IOException {
        String message = messageResolver.createMessage(baseMessage);


//        Socket socket = new Socket(node.getIp(), node.getPort());
//        OutputStream outputStream = socket.getOutputStream();
//        PrintWriter writer = new PrintWriter(outputStream, true);
//        writer.println(jsonMessage);
//        logger.info("Wysłano request do " + ip + ":" + port + " message: " + jsonMessage);
    }



}
