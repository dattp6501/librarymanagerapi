package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

import dao.MemberDAO;
import filters.SessionFilter;
import global.Init;
import model.Group;
import model.Member;
import model.MemberLogin;
import utils.JsonCustom;


@WebServlet(urlPatterns = {"/member/login","/member/register","/member/logout","/member/profile","/member/*"})
public class MemberAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        if(url.equals(host+"/member/login")){
            memberLogin(req,resp);
        }else if(url.equals(host+"/member/register")){
            memberRegister(req,resp);
        }else if(url.equals(host+"/member/logout")){
            memberLogout(req,resp);
        }else if(url.equals(host+"/member/profile")){
            memberProfile(req,resp);
        }else if(url.equals(host+"/member/update_profile")){
            updateProfile(req, resp);
        }
    }
    //-----------------------------------login-------------------------
    private void memberLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String username = objReq.getString("username");
            String password = objReq.getString("password");
            Member member = new Member(-1, null, null, username, password, new Group(),"");
            MemberDAO memberDAO = new MemberDAO();
            if(memberDAO.connect()){
                if(!memberDAO.checkLogin(member)) {// tai khoan hoac mat khau sai
                    resp1.put("code", 401);
                    resp1.put("description","Sai tên đăng nhập hoặc mật khẩu");
                } else {// tai khoan mat khau dung
                    MemberLogin memberLogin = new MemberLogin(member, null);
                    if(!addUserToListUserLogin(memberLogin)){
                        resp1.put("code",503);
                        resp1.put("description","Hệ thống đang quá tải! vui lòng vào lại sau ít phút :)");
                    }else{
                        memberLogin.setSession(generateSession());
                        resp1.put("code",200);
                        resp1.put("description", "Đăng nhập thành công");
                        resp1.put("session", memberLogin.getSession());
                    }
                }
                memberDAO.close();
            }else{
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
            }
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private boolean addUserToListUserLogin(MemberLogin memberLogin){
        if(Init.MEMBER_LOGINS.size() >= Init.MAX_MEMBER_LOGIN){// qua gioi hang bo nho he thong
            return false;
        }
        Init.MEMBER_LOGINS.add(memberLogin);
        return true;
    }
    private String generateSession(){
        UUID session = UUID.randomUUID();
        return session.toString();
    }
    //---------------------------------------register----------------------------
    private void memberRegister(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq =new JSONObject(JsonCustom.JsonToString(req.getReader()).toString());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String fullname = objReq.getString("fullname");
            String email = objReq.getString("email");
            String username = objReq.getString("username");
            String password = objReq.getString("password");
            String image = null;
            if(!checkEmail(email)){
                resp1.put("code",300);
                resp1.put("description","Sai định dạng email");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Member member = new Member(-1, email, fullname, username, password, Init.CUSTOMER_GROUP,image);
            MemberDAO memberDAO = new MemberDAO();
            if(!memberDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }  
            if(!memberDAO.add(member)){
                resp1.put("code",300);
                resp1.put("description","Không đăng kí được");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            resp1.put("code",200);
            resp1.put("description","Đăng kí thành công");
            memberDAO.close();
        } catch (Exception e) {
            resp1.put("code",300);
            String message = e.getMessage().toLowerCase();
            if(message.indexOf("duplicate")>=0){
                if(message.indexOf("'email'")>=0 || message.indexOf("'members.email'")>=0){
                    message = "Email đã được sử dụng";
                }else if(message.indexOf("'username'")>=0 || message.indexOf("'members.username'")>=0){
                    message = "Tên đã tồn tại";
                }
            }
            resp1.put("description",message);
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private boolean checkEmail(String email){
        /*
         * username@domain.com
         * user.name@domain.com
         * user-name@domain.com
         * username@domain.co.in
         * user_name@domain.com
         */
        String regex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" 
        + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regex).matcher(email).matches();
    }
    //-----------------------------------------logout----------------------------------
    private void memberLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String session = objReq.getString("session");
            int res = SessionFilter.checkSession(session);
            if(res==0){
                resp1.put("code", 500);
                resp1.put("description", "Người dùng chưa đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(res==2){
                resp1.put("code", 700);
                resp1.put("description", "Hết phiên đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!logout(session)){
                resp1.put("code",300);
                resp1.put("description","Đăng xuất thất bại");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            resp1.put("code",200);
            resp1.put("description","Đăng xuất thành công");
        }catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private boolean logout(String session){
        return Init.MEMBER_LOGINS.remove(new MemberLogin(null, session));
    }
    private void memberProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            MemberDAO memberDAO = new MemberDAO();
            if(!memberDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                memberDAO.close();
                return;
            }
            Member member = memberDAO.getMemberByID(memberLogin.getMember().getId()); memberDAO.close();
            if(member == null){
                resp1.put("code",300);
                resp1.put("description","Khách hàng không tồn tại");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject result = new JSONObject();
            JSONObject memJson = new JSONObject();
            memJson.put("mem_id",member.getId());
            memJson.put("mem_fullname",member.getFullName());
            memJson.put("mem_email",member.getEmail());
            memJson.put("mem_username",member.getUserName());
            memJson.put("mem_avatar",member.getImage());
            memJson.put("mem_group",member.getGroup().getName().toLowerCase());
            result.put("member",memJson);
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("result", result);
        }catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void updateProfile(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq =new JSONObject(JsonCustom.JsonToString(req.getReader()).toString());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            JSONObject memberJSON = objReq.getJSONObject("member");
            int id = memberJSON.getInt("mem_id");
            String fullname = memberJSON.getString("mem_fullname");
            String email = memberJSON.getString("mem_email");
            String username = memberJSON.getString("mem_username");
            String password = null;// chua dung toi
            String image = null;
            try{image = memberJSON.getString("mem_avatar");
            }catch(Exception e){}
            if(image==null || image.equals("")){
                image = null;
            }
            if(!checkEmail(email)){
                resp1.put("code",300);
                resp1.put("description","Sai định dạng email");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Member member = new Member(id, email, fullname, username, password, new Group(Init.CUSTOMER_GROUP.getId(), "CUSTOMER", ""),image);
            MemberDAO memberDAO = new MemberDAO();
            if(!memberDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }  
            if(!memberDAO.update(member)){
                resp1.put("code",300);
                resp1.put("description","Không cập nhật kí được");
                writer.println(resp1.toString());
                writer.close();
                memberDAO.close();
                return;
            }
            memberDAO.close();
            resp1.put("code",200);
            resp1.put("description","Cập nhật thành công");
            memberDAO.close();
        } catch (Exception e) {
            resp1.put("code",300);
            String message = e.getMessage().toLowerCase();
            if(message.indexOf("duplicate")>=0){
                if(message.indexOf("'email'")>=0 || message.indexOf("'members.email'")>=0){
                    message = "Email đã được sử dụng";
                }else if(message.indexOf("'username'")>=0 || message.indexOf("'members.username'")>=0){
                    message = "username đã tồn tại";
                }
            }
            resp1.put("description",message);
        }
        writer.println(resp1.toString());
        writer.close();
    }
}