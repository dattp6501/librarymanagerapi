package global;

import java.util.ArrayList;
import java.util.HashSet;

import model.Group;
import model.MemberLogin;

public class Init {
    public static int USER_NUMBER = 0;
    public static HashSet<String> SET_IP = new HashSet<>();
    public static final int MAX_MEMBER_LOGIN = 10;
    public static final long TIME = 2*60*60*1000;
    public static final ArrayList<MemberLogin> MEMBER_LOGINS = new ArrayList<>();

    // public static final String HOST = "";
    // public static final String JDBC = "jdbc:mysql://sql12.freemysqlhosting.net/sql12604853";
    // public static final String USER_NAME_DB = "sql12604853";
    // public static final String PASS_WORD_DB = "x2wc7WZhRv";

    public static final String HOST = "/librarymanagerapi";
    public static final String JDBC = "jdbc:mysql://localhost:3306/quanlysach";
    public static final String USER_NAME_DB = "dattp";
    public static final String PASS_WORD_DB = "dattp";

    public static final Group ADMIN_GROUP = new Group(1, "ADMIN", "");
    public static final Group CUSTOMER_GROUP = new Group(2, "CUSTOMER", "");
}