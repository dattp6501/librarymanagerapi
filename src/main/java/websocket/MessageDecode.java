package websocket;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class MessageDecode implements Decoder.Text<String> {

    @Override
    public void destroy() {
    }

    @Override
    public void init(EndpointConfig arg0) {
    }

    @Override
    public String decode(String message) throws DecodeException {
        return message;
    }

    @Override
    public boolean willDecode(String arg0) {
        return true;
    }
    
}
