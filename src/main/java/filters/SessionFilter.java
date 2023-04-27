package filters;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import dao.MemberDAO;
import model.MemberLogin;

public class SessionFilter{
    public static int checkSession(String session) throws SQLException, ParseException{
        //-1 khong ket noi duoc csdl
        //0 chua dang nhap
        //1 thanh cong
        //2 session het thoi gian
        // MemberLogin user = new MemberLogin(null, session);
        // int index = Init.MEMBER_LOGINS.indexOf(user);
        // if(index<0){
        //     return 0;
        // }
        // user = Init.MEMBER_LOGINS.get(index);
        long time_current = new Date().getTime();
        // if(time_current<user.getTime().getTime()){
        //     return 1;
        // }
        // Init.MEMBER_LOGINS.remove(user);
        // return 2;
        MemberDAO memberDAO = new MemberDAO();
        if(!memberDAO.connect()){
            return -1;
        }
        MemberLogin memL = memberDAO.getMemberLoginBySession(session);
        if(memL==null){
            memberDAO.close();
            return 0;
        }
        if(time_current < memL.getTime().getTime()){
            memberDAO.close();
            return 1;
        }
        memberDAO.DeleteMemberLoginBySession(memL);
        memberDAO.close();
        return 2;
    }

    public static MemberLogin checkMemberBySession(String session) throws SQLException, ParseException{
        // MemberLogin user = new MemberLogin(null, session);
        // // kiem tra xem da dang nhap hay chua
        // int index = Init.MEMBER_LOGINS.indexOf(user);
        // if(index<0){
        //     return null;
        // }
        // user = Init.MEMBER_LOGINS.get(index);
        // // kiem tra xem da het phien chua
        // long time_current = new Date().getTime();
        // if(time_current<user.getTime().getTime()){
        //     return user;
        // }
        // user.setTime(null);
        // Init.MEMBER_LOGINS.remove(user);
        // return user;
        MemberDAO memberDAO = new MemberDAO();
        if(!memberDAO.connect()){
            return null;
        }
        return memberDAO.getMemberLoginBySession(session);
    }
}
