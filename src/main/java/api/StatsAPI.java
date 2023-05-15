package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.BookDAO;
import dao.BookingDAO;
import dao.StatsDAO;
import filters.SessionFilter;
import global.Init;
import model.Booking;
import model.MemberLogin;
import utils.JsonCustom;
@WebServlet(urlPatterns = {"/stats/*"})
public class StatsAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        // admin
        if(url.equals(host+"/stats/stats_revenue_year")){
            StatsRevenueYear(req, resp);
        }else if(url.equals(host+"/stats/stats_booking_current_day")){
            StatsBookingCurrentDayAdmin(req, resp);
        }else if(url.equals(host+"/stats/stats_number_of_booking")){
            StatsNumberOfBooking(req, resp);
        }else if(url.equals(host+"/stats/stats_points_of_booking")){
            statsPointsOfBooking(req, resp);
        }
    }
    private void StatsRevenueYear(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            // kiem tra nhom
            if(!memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Không đủ quyền");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            int year = Calendar.getInstance().get(Calendar.YEAR);
            try {
                year = objReq.getInt("year");
            } catch (Exception e) {}
            StatsDAO statDAO = new StatsDAO();
            if(!statDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONArray list = statDAO.StatsRevenueYear(year);
            statDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
            resp1.put("list", list);
        } catch (Exception e) {
            resp1.put("code",300);
            String message = e.getMessage().toLowerCase();
            resp1.put("description",message.replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void StatsBookingCurrentDayAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            // kiem tra nhom
            if(!memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Không đủ quyền");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject result = new JSONObject();
            ArrayList<Booking> bookings = bookingDAO.getAllByDate(format.format(new Date())+" 00:00:00",null); bookingDAO.close();
            int cancelBooking = 0;
            int successBooking = 0;
            int waitBooking = 0;
            float revenueTheory = 0;
            float pay = 0;
            for(Booking bk : bookings){
                if(bk.getSuccess()==-1){
                    waitBooking++;
                    continue;
                }
                if(bk.getSuccess()==0){
                    cancelBooking++;
                    continue;
                }
                if(bk.getSuccess()==1){
                    successBooking++;
                    revenueTheory += bk.totalPrice();
                    pay += bk.getPay();
                    continue;
                }
            }
            result.put("total_booking", bookings.size());
            result.put("cancel_booking", cancelBooking);
            result.put("waiting_booking", waitBooking);
            result.put("success_booking", successBooking);
            result.put("revenue_theory", revenueTheory);
            result.put("revenue_real", pay);
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("result", result);
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void StatsNumberOfBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            // kiem tra nhom
            if(!memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Không đủ quyền");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // lấy dữ liệu
            Date from = null;
            Date to = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                from = format.parse(objReq.getString("date_from"));
                to = format.parse(objReq.getString("date_to"));
            } catch (Exception e) {
                Calendar calendarFrom = Calendar.getInstance();
                calendarFrom.set(Calendar.DATE, 1);
                from = calendarFrom.getTime();
                Calendar calendarTo = Calendar.getInstance();
                int maxDateOfMonth = calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendarTo.set(Calendar.DATE, maxDateOfMonth);
                to = calendarTo.getTime();
            }
            BookDAO bookDao = new BookDAO();
            if(!bookDao.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONArray list = bookDao.getListBookedDateRange1(from,to);
            bookDao.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
            resp1.put("list", list);
        } catch (Exception e) {
            resp1.put("code",300);
            String message = e.getMessage().toLowerCase();
            resp1.put("description",message.replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void statsPointsOfBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            // kiem tra nhom
            if(!memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Không đủ quyền");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            StatsDAO statsDAO = new StatsDAO();
            if(!statsDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject result = new JSONObject();
            result.put("type","FeatureCollection");
            result.put("features", statsDAO.getPointsBooking());
            statsDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
            resp1.put("result", result);
        } catch (Exception e) {
            resp1.put("code",300);
            String message = e.getMessage().toLowerCase();
            resp1.put("description",message.replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
}
