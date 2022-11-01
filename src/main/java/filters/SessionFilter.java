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
        return 2;
    }
}
