package websocket;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint(
    // encoders = MessageEncode.class, decoders = MessageDecode.class
)
public class ClientEndpointServer {
    @OnOpen
    public void onOpen(Session session){

    }
    
    @OnMessage
    public void OnMessage(String message){
        System.out.println("server : "+ message);
    }
}
