package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import filters.SessionFilter;
import global.Init;
import model.Book;
import model.Booked;
import model.Booking;
import model.Member;
import model.MemberLogin;
import model.VoucherBooking;
import utils.JsonCustom;
import utils.Mail;


@WebServlet(urlPatterns = {"/boooking/*"})
public class BookingAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        if(url.equals(host+"/boooking/add_booking")){
            addBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_booking")){
            getBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_all_booking_by_date")){
            getAllBookingByDateAdmin(req, resp);
        }else if(url.equals(host+"/boooking/confirm_booking")){
            confirmBooking(req, resp);
        }else if(url.equals(host+"/boooking/update_booking")){
            updateBooking(req, resp);
        }else if(url.equals(host+"/boooking/cancel_booking")){
            cancelBooking(req, resp);
        }else if(url.equals(host+"/boooking/add_voucher_booking")){
            addVoucherBooking(req, resp);
        }else if(url.equals(host+"/boooking/close_voucher_booking")){
            CloseVoucherBooking(req, resp);
        }else if(url.equals(host+"/boooking/open_voucher_booking")){
            openVoucherBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_all_voucher_booking")){
            getAllVoucherBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_voucher_booking_active")){
            getVoucherBookingActive(req, resp);
        }
    }

    private JSONArray getStatusBooking(){
        JSONArray listJSON = new JSONArray();
        JSONObject statusJSON1 = new JSONObject();
        statusJSON1.put("mode",-1);
        statusJSON1.put("name","Đang xử lý");
        listJSON.put(statusJSON1);
        JSONObject statusJSON2 = new JSONObject();
        statusJSON2.put("mode",0);
        statusJSON2.put("name","Đã hủy");
        listJSON.put(statusJSON2);
        JSONObject statusJSON3 = new JSONObject();
        statusJSON3.put("mode",1);
        statusJSON3.put("name","Thành công");
        listJSON.put(statusJSON3);
        return listJSON;
    }

    private void addBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String session = objReq.getString("session");
            MemberLogin memberLogin = SessionFilter.checkMemberBySession(session);
            if(memberLogin==null){
                resp1.put("code", 400);
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
            // cart
            JSONObject cartJSON = objReq.getJSONObject("cart");
            Member member = memberLogin.getMember();
            Date bookingDate = new Date();
            ArrayList<Booked> bookeds = new ArrayList<>();
            String bookingNote = "";
            try{bookingNote=cartJSON.getString("cart_note");}catch(Exception e){}
            for(Object bookedJSON : cartJSON.getJSONArray("books")){
                Book book = new Book();
                book.setId(((JSONObject)bookedJSON).getInt("book_id"));
                // 
                float bookedPrice = ((JSONObject)bookedJSON).getFloat("book_price");
                int bookedNumber = ((JSONObject)bookedJSON).getInt("book_number");
                String bookedNote = ((JSONObject)bookedJSON).getString("book_note");
                bookeds.add(new Booked(book, bookedPrice, bookedNumber, bookedNote));
            }
            // voucher
            ArrayList<VoucherBooking> voucherBookings = new ArrayList<>();
            try {
                JSONArray vouchersJSON = objReq.getJSONArray("voucher_booking");
                for(Object vJSON : vouchersJSON){
                    if(((JSONObject)vJSON).getInt("voucher_booking_id")<=0){
                        continue;
                    }
                    VoucherBooking v = new VoucherBooking();
                    v.setId(((JSONObject)vJSON).getInt("voucher_booking_id"));
                    voucherBookings.add(v);
                }
            }catch(Exception e){
                voucherBookings.clear();
            }
            // booking
            String address = objReq.getString("address");
            Booking booking = new Booking(-1, bookingDate, member, bookeds, bookingNote,-1, 0, address, voucherBookings);
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            for(VoucherBooking v : booking.getVoucherBookings()){
                if(v.getId()<=0){
                    booking.getVoucherBookings().remove(v);
                    continue;
                }
                VoucherBooking voucherTmp = bookingDAO.getVoucherByID(v.getId());
                if(voucherTmp==null || voucherTmp.getActive()<=0){
                    resp1.put("code",300);
                    resp1.put("description","Voucher "+v.getName()+" Không tồn tại");
                    writer.println(resp1.toString());
                    writer.close();
                    bookingDAO.close();
                    return;
                }
                v.setName(voucherTmp.getName());
                v.setValue(voucherTmp.getValue());
                v.setType(voucherTmp.getType());
            }
            BookDAO bookDAO = new BookDAO(); bookDAO.setConnection(bookingDAO.getConnection());
            for(Booked booked : booking.getBookeds()){
                if(bookDAO.getBookByID(booked.getBook().getId())==null){
                    resp1.put("code",300);
                    resp1.put("description","Không được đặt sách không tồn tại");
                    writer.println(resp1.toString());
                    writer.close();
                    bookingDAO.close();
                    return;
                }
            }
            if(!bookingDAO.addBooking(booking)){
                resp1.put("code",300);
                resp1.put("description","Không đặt được sách");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            resp1.put("code",200);
            resp1.put("description","Thành công");
            try {
                Mail.sendMail(memberLogin.getMember().getEmail(),Mail.createrFormBookingSuccess(bookingDAO.getBookingByID(booking.getId())));
            } catch (Exception e1) {e1.printStackTrace();}
            bookingDAO.close();
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void getBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject result = new JSONObject();
            JSONArray listJson = new JSONArray();
            ArrayList<Booking> bookings = bookingDAO.getByMemberID(memberLogin.getMember().getId());
            bookingDAO.close();
            for(Booking bk : bookings){
                JSONObject bookingJSON = new JSONObject();
                bookingJSON.put("booking_id",bk.getId());
                bookingJSON.put("booking_date",bk.getDateStrMail());
                bookingJSON.put("booking_note",bk.getNote());
                bookingJSON.put("booking_total_price", bk.totalPrice());
                bookingJSON.put("booking_success", bk.getSuccess());
                bookingJSON.put("booking_pay",bk.getPay());
                // voucher
                JSONArray listVoucherJSON = new JSONArray();
                for(VoucherBooking v : bk.getVoucherBookings()){
                    JSONObject vJSON = new JSONObject();
                    vJSON.put("voucher_booking_id",v.getId());
                    vJSON.put("voucher_booking_name",v.getName());
                    vJSON.put("voucher_booking_type",v.getType());
                    vJSON.put("voucher_booking_value",v.getValue());
                    vJSON.put("voucher_booking_note",v.getNote());
                    vJSON.put("voucher_booking_active",v.getActive());
                    listVoucherJSON.put(vJSON);
                }
                bookingJSON.put("voucher_booking", listVoucherJSON);
                // 
                JSONArray listBookedJSON = new JSONArray();
                for(Booked booked : bk.getBookeds()){
                    JSONObject bookedJSON = new JSONObject();
                    bookedJSON.put("booked_price",booked.getPrice());
                    bookedJSON.put("booked_number",booked.getNumber());
                    bookedJSON.put("booked_note",booked.getNote());
                    if(booked.getBook()!=null){
                        JSONObject bookJSON = new JSONObject();
                        bookJSON.put("book_id", booked.getBook().getId());
                        bookJSON.put("book_title", booked.getBook().getTitle());
                        bookJSON.put("book_author", booked.getBook().getAuthor());
                        bookJSON.put("book_type", booked.getBook().getType());
                        bookJSON.put("book_release_date", booked.getBook().getReleaseDateFormat());
                        bookJSON.put("book_page_number", booked.getBook().getPageNumber());
                        bookJSON.put("book_image", booked.getBook().getImage());
                        bookJSON.put("book_description", booked.getBook().getDescription());
                        bookedJSON.put("book",bookJSON);
                    }
                    listBookedJSON.put(bookedJSON);
                }
                bookingJSON.put("bookeds",listBookedJSON);

                listJson.put(bookingJSON);
            }
            result.put("total",listJson.length());
            result.put("list", listJson);
            result.put("status",getStatusBooking());
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

    private void getAllBookingByDateAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // lay du lieu gui den
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateFrom = objReq.getString("date");
            String dateTo = null;
            format.parse(dateFrom);// kiem tra tinh hop le cua date
            try{dateTo = objReq.getString("date_to");}catch(Exception e){}
            if(dateTo!=null){
                format.parse(dateTo);
            }
            // 
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject result = new JSONObject();
            JSONArray listJson = new JSONArray();
            ArrayList<Booking> bookings = bookingDAO.getAllByDate(dateFrom,dateTo); bookingDAO.close();
            for(Booking bk : bookings){
                JSONObject bookingJSON = new JSONObject();
                bookingJSON.put("booking_id",bk.getId());
                bookingJSON.put("booking_date",bk.getDateStrMail());
                bookingJSON.put("booking_note",bk.getNote());
                bookingJSON.put("booking_total_price", bk.totalPrice());
                bookingJSON.put("booking_success", bk.getSuccess());
                bookingJSON.put("booking_pay",bk.getPay());
                // voucher
                JSONArray listVoucherJSON = new JSONArray();
                for(VoucherBooking v : bk.getVoucherBookings()){
                    JSONObject vJSON = new JSONObject();
                    vJSON.put("voucher_booking_id",v.getId());
                    vJSON.put("voucher_booking_name",v.getName());
                    vJSON.put("voucher_booking_type",v.getType());
                    vJSON.put("voucher_booking_value",v.getValue());
                    vJSON.put("voucher_booking_note",v.getNote());
                    vJSON.put("voucher_booking_active",v.getActive());
                    listVoucherJSON.put(vJSON);
                }
                bookingJSON.put("voucher_booking", listVoucherJSON);
                // 
                // member
                JSONObject memJSON = new JSONObject();
                memJSON.put("member_id",bk.getMember().getId());
                memJSON.put("member_fullname",bk.getMember().getFullName());
                memJSON.put("member_username",bk.getMember().getUserName());
                memJSON.put("member_email",bk.getMember().getEmail());
                bookingJSON.put("member",memJSON);
                JSONArray listBookedJSON = new JSONArray();
                for(Booked booked : bk.getBookeds()){
                    JSONObject bookedJSON = new JSONObject();
                    bookedJSON.put("booked_price",booked.getPrice());
                    bookedJSON.put("booked_number",booked.getNumber());
                    bookedJSON.put("booked_note",booked.getNote());
                    if(booked.getBook()!=null){
                        JSONObject bookJSON = new JSONObject();
                        bookJSON.put("book_id", booked.getBook().getId());
                        bookJSON.put("book_title", booked.getBook().getTitle());
                        bookJSON.put("book_author", booked.getBook().getAuthor());
                        bookJSON.put("book_type", booked.getBook().getType());
                        bookJSON.put("book_release_date", booked.getBook().getReleaseDateFormat());
                        bookJSON.put("book_page_number", booked.getBook().getPageNumber());
                        bookJSON.put("book_image", booked.getBook().getImage());
                        bookJSON.put("book_description", booked.getBook().getDescription());
                        bookedJSON.put("book",bookJSON);
                    }
                    listBookedJSON.put(bookedJSON);
                }
                bookingJSON.put("bookeds",listBookedJSON);
                listJson.put(bookingJSON);
            }
            result.put("total",listJson.length());
            result.put("list", listJson);
            result.put("status", getStatusBooking());
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

    private void confirmBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // lay du lieu
            int booingID = objReq.getJSONObject("booking").getInt("booking_id");
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Booking booking = bookingDAO.getBookingByID(booingID);
            if(booking==null){
                resp1.put("code",300);
                resp1.put("description","không có phiếu đặt sách nào");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            if(booking.getSuccess()==0){
                resp1.put("code",300);
                resp1.put("description","Đơn hàng đã được hủy");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            if(booking.getSuccess()==1){
                resp1.put("code",300);
                resp1.put("description","Đơn hàng đã được xác nhận");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            for(Booked booked : booking.getBookeds()){
                if(booked.getNumber()>booked.getBook().getNumber()){
                    resp1.put("code",300);
                    String meessage = String.format("Số lượng sách %s không đủ, chỉ còn %d",booked.getBook().getTitle().toLowerCase(), booked.getBook().getNumber());
                    resp1.put("description",meessage);
                    writer.println(resp1.toString());
                    writer.close();
                    bookingDAO.close();
                    return;
                }
            }
            if(!bookingDAO.confirmBooking(booking)){
                resp1.put("code",300);
                resp1.put("description","Xác nhận đơn hàng không thành công");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
            // try {
            //     Mail.sendMail(booking.getMember().getEmail(), "Đơn hàng");
            // } catch (Exception e1) {e1.printStackTrace();}
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void cancelBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // lay du lieu
            int booingID = objReq.getJSONObject("booking").getInt("booking_id");
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Booking booking = bookingDAO.getBookingByID(booingID);
            if(booking==null){
                resp1.put("code",300);
                resp1.put("description","không có phiếu đặt sách nào");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            if(booking.getSuccess()==0){
                resp1.put("code",300);
                resp1.put("description","Đơn hàng đã được hủy từ trước đây");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            if(booking.getSuccess()==1){
                resp1.put("code",300);
                resp1.put("description","Đơn hàng đã được xác nhận, Không thể hủy được");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }

            if(!bookingDAO.updateStatusBooking(booking.getId(),0)){
                resp1.put("code",300);
                resp1.put("description","Đơn hàng không hủy được");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void updateBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // kiem trqa phieu dat
            int bookingID = objReq.getJSONObject("booking").getInt("booking_id");
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Booking bookingOld = bookingDAO.getBookingByID(bookingID);
            if(bookingOld==null){
                resp1.put("code",300);
                resp1.put("description","không có phiếu đặt sách nào");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            // lay du lieu
            ArrayList<Booked> bookeds = new ArrayList<>();
            for(Object bookedJSON : objReq.getJSONObject("booking").getJSONArray("bookeds")){
                Book book = new Book();
                book.setId(((JSONObject)bookedJSON).getInt("book_id"));
                // 
                float bookedPrice = ((JSONObject)bookedJSON).getFloat("book_price");// khong dung den
                int bookedNumber = ((JSONObject)bookedJSON).getInt("book_number");
                String bookedNote = ((JSONObject)bookedJSON).getString("book_note");
                bookeds.add(new Booked(book, bookedPrice, bookedNumber, bookedNote));
            }
            // 
            String address = objReq.getString("address");
            Booking booking = new Booking(bookingID, null, null, bookeds, null,-1, 0, address,null);
            resp1.put("code",200);
            resp1.put("description","Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void addVoucherBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // 
            JSONObject voucherJSON = objReq.getJSONObject("voucher_booking");
            String voucherName = voucherJSON.getString("voucher_booking_name");
            String voucherType = voucherJSON.getString("voucher_booking_type");// %,n,...
            float voucherValue = voucherJSON.getFloat("voucher_booking_value");
            String voucherNote = null;
            try{voucherNote = voucherJSON.getString("voucher_booking_note");
            }catch(Exception e){}
            Date voucherCreatedDate = new Date();
            if(voucherValue<=0){
                resp1.put("code", 300);
                resp1.put("description", "Giá trị giảm giá phải > 0");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            VoucherBooking voucherBooking = new VoucherBooking(-1, voucherName, voucherType, voucherNote, voucherValue, 0, voucherCreatedDate);
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookingDAO.addVoucher(voucherBooking)){
                resp1.put("code",300);
                resp1.put("description","Không tạo được voucher");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void CloseVoucherBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // 
            JSONObject voucherJSON = objReq.getJSONObject("voucher_booking");
            int voucherID = voucherJSON.getInt("voucher_booking_id");
            VoucherBooking voucher = new VoucherBooking();
            voucher.setId(voucherID);
            voucher.setActive(0);
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookingDAO.setActiveVoucher(voucher)){
                resp1.put("code",300);
                resp1.put("description","Không đóng voucher được");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void openVoucherBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // 
            JSONObject voucherJSON = objReq.getJSONObject("voucher_booking");
            int voucherID = voucherJSON.getInt("voucher_booking_id");
            VoucherBooking voucher = new VoucherBooking();
            voucher.setId(voucherID);
            voucher.setActive(1);
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookingDAO.setActiveVoucher(voucher)){
                resp1.put("code",300);
                resp1.put("description","Không mở voucher được");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void getAllVoucherBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // 
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // voucher
            JSONObject result = new JSONObject();
            ArrayList<VoucherBooking> voucherBookings = bookingDAO.getAllVoucher(); bookingDAO.close();
            JSONArray listVoucherJSON = new JSONArray();
            for(VoucherBooking v : voucherBookings){
                JSONObject vJSON = new JSONObject();
                vJSON.put("voucher_booking_id",v.getId());
                vJSON.put("voucher_booking_name",v.getName());
                vJSON.put("voucher_booking_type",v.getType());
                vJSON.put("voucher_booking_value",v.getValue());
                vJSON.put("voucher_booking_note",v.getNote());
                vJSON.put("voucher_booking_created_date",v.getCreatedDateStr());
                vJSON.put("voucher_booking_active",v.getActive());
                listVoucherJSON.put(vJSON);
            }
            result.put("list",listVoucherJSON);
            result.put("total",listVoucherJSON.length());
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
    private void getVoucherBookingActive(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
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
            // 
            BookingDAO bookingDAO = new BookingDAO();
            if(!bookingDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // voucher
            JSONObject result = new JSONObject();
            ArrayList<VoucherBooking> voucherBookings = bookingDAO.getVoucherActive(); bookingDAO.close();
            JSONArray listVoucherJSON = new JSONArray();
            for(VoucherBooking v : voucherBookings){
                JSONObject vJSON = new JSONObject();
                vJSON.put("voucher_booking_id",v.getId());
                vJSON.put("voucher_booking_name",v.getName());
                vJSON.put("voucher_booking_type",v.getType());
                vJSON.put("voucher_booking_value",v.getValue());
                vJSON.put("voucher_booking_note",v.getNote());
                vJSON.put("voucher_booking_created_date",v.getCreatedDateStr());
                vJSON.put("voucher_booking_active",v.getActive());
                listVoucherJSON.put(vJSON);
            }
            result.put("list",listVoucherJSON);
            result.put("total",listVoucherJSON.length());
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
}
