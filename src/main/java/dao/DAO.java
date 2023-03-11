package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import global.Init;

public class DAO {
    private final String url = Init.JDBC;
    private final String username = Init.USER_NAME_DB;
    private final String password = Init.PASS_WORD_DB;
    protected Connection connection = null;
    public DAO() {
        super();
    }
    public boolean connect(){
        boolean resp = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            // Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // connection = DriverManager.getConnection("jdbc:sqlserver://sql111.epizy.com:3306;databaseName=epiz_33620435_epiz_33620435_;user=epiz_33620435;password=WenMKRNqFAE");
            resp = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return resp;
    }
    public boolean close(){
        boolean resp = false;
        try {
            connection.close();
            resp = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resp;
    }
    public String getUrl() {
        return url;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public Connection getConnection() {
        return connection;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    public static void main(String[] args) {
        DAO dao = new DAO();
        if(!dao.connect()){
            System.out.println("Khong ket noi duoc CSDL");
            return;
        }
        dao.close();
    }
}