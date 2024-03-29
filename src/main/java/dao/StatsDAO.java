package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.DateCustom;

public class StatsDAO extends DAO{
    public StatsDAO(){}
    public JSONArray StatsRevenueYear(int year) throws SQLException{
        JSONArray list = new JSONArray();
        PreparedStatement ps = null;
        for(int month=1; month<=12; month++){
            // tong trong thang
            String select = "SELECT * FROM booking WHERE ?<=date and date<=? and success=1";// 0 da huy, -1: dang xu ly, 1: thanh cong
            ps = connection.prepareStatement(select);
            int maxDay = DateCustom.getDateNumberOfMonth(month, year);
            ps.setString(1, year+"-"+month+"-"+"1 00:00:00");
            ps.setString(2, year+"-"+month+"-"+maxDay+" 23:59:59");
            ResultSet res = ps.executeQuery();
            int totalTheory = 0;//trên lý thuyết
            int totalReal = 0; //trên thực tế
            int totalPay = 0;//tiền đã được thanh toán
            while(res.next()){
                totalTheory+= res.getFloat("total_price");
                totalPay += res.getFloat("pay");
            }
            totalReal = totalPay;
            // ps.close();
            // danh sach cac ngay
            JSONArray days = new JSONArray();
            for(int day=1; day<=maxDay; day++){
                ps.setString(1, year+"-"+month+"-"+day+" 00:00:00");
                ps.setString(2, year+"-"+month+"-"+day+" 23:59:59");
                res = ps.executeQuery();
                int totalTheory1 = 0;//trên lý thuyết
                int totalReal1 = 0; //trên thực tế
                int totalPay1 = 0;//tiền đã được thanh toán
                while(res.next()){
                    totalTheory1+= res.getFloat("total_price");
                    totalPay1 += res.getFloat("pay");
                }
                totalReal1 = totalPay1;
                JSONObject d1 = new JSONObject();
                d1.put("revenue_theory", totalTheory1);
                d1.put("revenue_real", totalReal1);
                d1.put("date",String.format("%02d/%02d/%d",day,month,year));
                days.put(d1);
                res.close();
            }
            // return
            JSONObject d = new JSONObject();
            d.put("revenue_theory", totalTheory);
            d.put("revenue_real", totalReal);
            d.put("month",String.format("%02d/%d",month,year));
            d.put("day", days);
            list.put(d);
            ps.close();
        }
        return list;
    }
    public JSONArray getPointsBooking() throws SQLException{
        JSONArray list = new JSONArray();
        String sql = "SELECT * FROM booking";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            if(res.getString("longg")==null || res.getString("longg").equals("null") || res.getString("longg").equals("")){
                continue;
            }
            JSONObject point = new JSONObject();
            point.put("type", "Feature");
            JSONObject geometry = new JSONObject();
            geometry.put("type","Point");
            geometry.put("coordinates", new JSONArray("["+res.getDouble("longg")+","+res.getDouble("latt")+","+0+"]"));
            point.put("geometry", geometry);
            list.put(point);
        }
        return list;
    }
    public static void main(String[] args) throws JSONException, SQLException {
        StatsDAO dao = new StatsDAO();
        if(dao.connect()){
            System.out.println(dao.getPointsBooking().get(0));
            dao.close();
        }
    }
}
