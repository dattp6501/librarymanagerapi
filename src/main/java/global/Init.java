package global;

import java.util.ArrayList;
import java.util.HashSet;

import model.Group;
import model.MemberLogin;

public class Init{
    public static final String port="80";
    public static int USER_NUMBER = 0;
    public static HashSet<String> SET_IP = new HashSet<>();
    public static final int MAX_MEMBER_LOGIN = 10;
    public static final long TIME = 2*60*60*1000;
    public static final ArrayList<MemberLogin> MEMBER_LOGINS = new ArrayList<>();

    public static final String HOST = "/librarymanagerapi";
    public static final String JDBC = "jdbc:mysql://localhost:3306/quanlysach";
    public static final String USER_NAME_DB = "dattp";
    public static final String PASS_WORD_DB = "dattp";
    // group
    public static final Group ADMIN_GROUP = new Group(1, "ADMIN", "");
    public static final Group CUSTOMER_GROUP = new Group(2, "CUSTOMER", "");
    // socket
    public static final String SESSION_KEY_WEBSOCKET = "xxxxx-yyyyy-zzzzz-dddddd-mmmmm";
    // public static final WebSocket SOCKET = null;

    // vnpay
    public static final String vnp_TmnCodeSource = "RT0C78GS";// dang ky moi co 9704198526191432190
    public static final String vnp_HashSecret = "BQOFKEPDYNSHCWIKOTPJDFZGEGYNDJTG";// dang ky moi co
    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";// trang thanh toan
    public static final String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    public static final String vnp_Returnurl = "http://localhost:"+port+"/librarymanagerapi/boooking/payment_return";
    // mapbox
    public static final String ACCESS_TOKEN_MAPBOX = "pk.eyJ1IjoiZGF0dHA2NTAxIiwiYSI6ImNsNWhyMndieDAwdnAzZG41ZWU0aWE4dGcifQ.QqoPZxARov6HykuFnXvLWg";
    
}