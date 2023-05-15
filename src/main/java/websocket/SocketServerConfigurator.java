package websocket;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.*;

import filters.SessionFilter;
import global.Init;
import model.MemberLogin;
// https://www.youtube.com/watch?v=YE8my5AF67s
public class SocketServerConfigurator extends ServerEndpointConfig.Configurator{

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response){
        List<String> parameter = request.getParameterMap().get("session");
        String session = parameter.get(0);
        MemberLogin memberLogin = null;
        try {
            // if(session.equals(Init.SESSION_KEY_WEBSOCKET)){
            //     sec.getUserProperties().put("session",session);
            //     return;
            // }
            memberLogin = SessionFilter.checkMemberBySession(session);
            if(memberLogin!=null){
                sec.getUserProperties().put("session",session);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }
}
