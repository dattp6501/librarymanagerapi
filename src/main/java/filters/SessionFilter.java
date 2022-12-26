package filters;

import java.util.Date;

import global.Init;
import model.MemberLogin;

public class SessionFilter{
    public static int checkSession(String session){
        //0 chua dang nhap
        //1 thanh cong
        //2 session het thoi gian
        MemberLogin user = new MemberLogin(null, session);
        int index = Init.MEMBER_LOGINS.indexOf(user);
        if(index<0){
            return 0;
        }
        user = Init.MEMBER_LOGINS.get(index);
        long time_current = new Date().getTime();
        if(time_current<user.getTime().getTime()){
            return 1;
        }
        Init.MEMBER_LOGINS.remove(user);
        return 2;
    }

    public static MemberLogin checkMemberBySession(String session){
        MemberLogin user = new MemberLogin(null, session);
        // kiem tra xem da dang nhap hay chua
        int index = Init.MEMBER_LOGINS.indexOf(user);
        if(index<0){
            return null;
        }
        user = Init.MEMBER_LOGINS.get(index);
        // kiem tra xem da het phien chua
        long time_current = new Date().getTime();
        if(time_current<user.getTime().getTime()){
            return user;
        }
        user.setTime(null);
        Init.MEMBER_LOGINS.remove(user);
        return user;
    }
}
