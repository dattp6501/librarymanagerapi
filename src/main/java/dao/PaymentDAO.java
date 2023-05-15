package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentDAO extends DAO{
    public PaymentDAO(){
        super();
    }
    public PaymentDAO(Connection connection){
        super(connection);
    }
    public boolean saveTransaction(JSONObject data) throws SQLException, JSONException, ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        String sql = "INSERT INTO transaction_history(member_id,booking_id,date,amount,payment_type) VALUES(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, data.getInt("member_id"));
        ps.setInt(2, data.getInt("booking_id"));
        ps.setString(3, format.format(format1.parse(data.getString("date"))));
        ps.setFloat(4, data.getLong("amount"));
        ps.setString(5, data.getString("payment_type"));
        boolean ok = ps.executeUpdate()>0;
        ps.close();
        return ok;
    }
    public JSONObject getTransaction(int bookingId, String date) throws SQLException{
        String select = "SELECT * FROM transaction_history WHERE booking_id=? AND date=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, bookingId);
        ps.setString(2, date);
        ResultSet res = ps.executeQuery();
        if(!res.next()){
            res.close();
            ps.close();
            return null;
        }
        JSONObject data = new JSONObject();
        data.put("member_id", res.getInt("member_id"));
        data.put("booking_id", res.getInt("booking_id"));
        data.put("date", res.getString("date"));
        data.put("amount", res.getFloat("amount"));
        data.put("payment_type", res.getString("payment_type"));
        res.close();
        ps.close();
        return data;
    }
    public JSONArray getHistoryTransactionByMemberID(int memberID, String dateFrom) throws SQLException{
        JSONArray list = new JSONArray();
        String select = "SELECT * FROM transaction_history WHERE member_id=? AND ?<=date ORDER BY date DESC";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, memberID);
        ps.setString(2, dateFrom);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            JSONObject data = new JSONObject();
            // data.put("member_id", res.getInt("member_id"));
            data.put("booking_id", res.getInt("booking_id"));
            data.put("date", res.getString("date"));
            data.put("amount", res.getFloat("amount"));
            data.put("payment_type", res.getString("payment_type"));
            list.put(data);
        }
        res.close();
        ps.close();
        return list;
    }
    public JSONArray getAllHistoryTransaction(String dateFrom) throws SQLException{
        JSONArray list = new JSONArray();
        String select = "SELECT * FROM transaction_history HWERE ?<=date ORDER BY date DESC";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setString(1, dateFrom);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            JSONObject data = new JSONObject();
            data.put("member_id", res.getInt("member_id"));
            data.put("booking_id", res.getInt("booking_id"));
            data.put("date", res.getString("date"));
            data.put("amount", res.getFloat("amount"));
            data.put("payment_type", res.getString("payment_type"));
            list.put(data);
        }
        res.close();
        ps.close();
        return list;
    }
    public static void main(String[] args) throws SQLException {
        PaymentDAO dao = new PaymentDAO();
        if(dao.connect()){
            System.out.println(dao.getHistoryTransactionByMemberID(6,"2023-05-08 21:43:00"));
            dao.close();
        }
    }
}