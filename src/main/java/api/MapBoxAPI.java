package api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import filters.SessionFilter;
import global.Init;
import model.MemberLogin;
import utils.JsonCustom;


@WebServlet(urlPatterns = { "/mapbox/*" })
public class MapBoxAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        // admin
        if (url.equals(host + "/mapbox/get_infor_mapbox")){
            getInforMapbox(req, resp);
        }
    }
    private void getInforMapbox(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
            String session = objReq.getString("session");
            MemberLogin memberLogin = SessionFilter.checkMemberBySession(session);
            if(memberLogin==null){
                resp1.put("code", 500);
                resp1.put("description", "Người dùng chưa đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(memberLogin.getTime()==null){
                resp1.put("code", 700);
                resp1.put("description", "Hết phiên đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // kiem tra nhom
            if(!memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Không đủ quyền");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // lay du lieu gui den
            JSONArray styles = new JSONArray();
            JSONObject style1 = new JSONObject();
            style1.put("name","Bản đồ đường");
            style1.put("value","mapbox://styles/mapbox/streets-v11");
            styles.put(style1);
            JSONObject style2 = new JSONObject();
            style2.put("name","bản đồ vệ tinh");
            style2.put("value","mapbox://styles/dattp6501/cl5htayk0000t14o5ljc3hqo5");
            styles.put(style2);
            JSONObject style3 = new JSONObject();
            style3.put("name","Bản đồ mới");
            style3.put("value","mapbox://styles/dattp6501/cl5htjpjx005e14nvbnwthjso");
            styles.put(style3);
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("key", Init.ACCESS_TOKEN_MAPBOX);
            resp1.put("styles",styles);
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
}
