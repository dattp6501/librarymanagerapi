package websocket;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import filters.SessionFilter;
import global.Init;
import model.MemberLogin;

@ServerEndpoint(value = "/socket_server",configurator = SocketServerConfigurator.class
// , encoders = MessageEncode.class, decoders = MessageDecode.class
)
public class SocketServerEndpoint {
    public static Set<Session> custemers = Collections.synchronizedSet(new HashSet<Session>());
    public static Set<Session> managers = Collections.synchronizedSet(new HashSet<Session>());
    @OnOpen
    public void handleOpen(EndpointConfig endpointConfig, Session userSession){
        try {
            MemberLogin memberLogin;
            String session = (String) endpointConfig.getUserProperties().get("session");
            // if(session.equals(Init.SESSION_KEY_WEBSOCKET)){
            //     managers.add(userSession);
            //     System.out.println(session + " đã kết nới với socket server");
            //     return;
            // }else{
                memberLogin = SessionFilter.checkMemberBySession(session);
                if(memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){//Exception này để block những sesion không tồn tại
                    managers.add(userSession);
                }else if(memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                    custemers.add(userSession);
                }
                if(memberLogin!=null){
                    System.out.println(session + " đã kết nới với socket server");
                }
            // }
           
            System.out.println("------------->>>>>>>>>>>>> number user: "+ managers.size()+" manager, "+ custemers.size()+" custemer");
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }
    @OnMessage
    public void handleMessage(String message, Session userSession){
        String session = (String) userSession.getUserProperties().get("session");
        if(session == null){
            return;
        }
        try {
            JSONObject reqData = new JSONObject(message);
            MemberLogin memberLogin = SessionFilter.checkMemberBySession(session);
            // cac chuc nang cua quan ly
            if(memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                JSONArray group = reqData.getJSONArray("group");
                JSONArray listAPI = reqData.getJSONArray("list_api");

                JSONObject respData = new JSONObject();
                try {
                    respData.put("code", 200);
                    respData.put("list_api",listAPI);
                    respData.put("description", "Thành công");
                } catch (Exception e) {
                    respData.put("code", 300);
                    respData.put("description", e.getMessage());
                }
                for(Object grouqJ : group){
                    if((int)grouqJ == Init.ADMIN_GROUP.getId()){
                        sendMessage(managers, respData.toString());
                    }else if((int)grouqJ == Init.CUSTOMER_GROUP.getId()){
                        sendMessage(custemers, respData.toString());
                    }
                }
                return ;
            }
            // cac chuc nang cua khach hang
            if(memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                JSONArray group = reqData.getJSONArray("group");
                JSONArray listAPI = reqData.getJSONArray("list_api");

                JSONObject respData = new JSONObject();
                try {
                    respData.put("code", 200);
                    respData.put("list_api",listAPI);
                    respData.put("description", "Thành công");
                } catch (Exception e) {
                    respData.put("code", 300);
                    respData.put("description", e.getMessage());
                }
                for(Object grouqJ : group){
                    if((int)grouqJ == Init.ADMIN_GROUP.getId()){
                        sendMessage(managers, respData.toString());
                    }else if((int)grouqJ == Init.CUSTOMER_GROUP.getId()){
                        sendMessage(custemers, respData.toString());
                    }
                }
                
                return ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendMessage(Set<Session> set, String message){
        set.stream().forEach(x ->{
            try {
                x.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @OnClose
    public void handleClose(Session userSession){
        System.out.println(userSession.getUserProperties().get("session")+" Đã ngắt kết nốt với socket server");
        custemers.remove(userSession);
    }

    @OnError
    public void handleError(Throwable t){
    }
}
