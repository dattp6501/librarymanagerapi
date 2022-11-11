package global;

import java.util.ArrayList;
import java.util.HashSet;

import model.MemberLogin;

public class Init {
    public static int USER_NUMBER = 0;
    public static HashSet<String> SET_IP = new HashSet<>();
    public static final int MAX_MEMBER_LOGIN = 10;
    public static final long TIME = 2*60*60*1000;
    public static final ArrayList<MemberLogin> MEMBER_LOGINS = new ArrayList<>();
    public static final String HOST = "";//      "/librarymanagerapi"
    public static final String JDBC = "jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12558616";//"jdbc:mysql://localhost:3306/quanlysach";
    public static final String USER_NAME_DB = "sql12558616";
    public static final String PASS_WORD_DB = "5vbmDVJIuf";
}