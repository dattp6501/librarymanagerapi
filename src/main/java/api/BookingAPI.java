package api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dao.BookDAO;
import dao.BookingDAO;
import dao.PaymentDAO;
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
import utils.MapBoxUtil;
import utils.vnpay;


@WebServlet(urlPatterns = {"/boooking/*"})
public class BookingAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        // 
        if(url.equals(host+"/boooking/get_list_payment_type")){
            getListPaymentType(req, resp);
        }else if(url.equals(host+"/boooking/get_list_booking_status")){
            getListBookingStatus(req, resp);
        }
        // custemer
        else if(url.equals(host+"/boooking/add_booking")){
            addBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_amount_booking")){
            getAmountBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_booking")){
            getBooking(req, resp);
        }else if(url.equals(host+"/boooking/get_voucher_booking_active")){
            getVoucherBookingActive(req, resp);
        }else if(url.equals(host+"/boooking/get_history_transaction")){
            getHistoryTransaction(req, resp);
        }else if(url.equals(host+"/boooking/get_history_transaction_detail")){
            getHistoryTransactionDetail(req, resp);
        }
        // admin
        else if(url.equals(host+"/boooking/get_all_booking_by_date")){
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
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            if(!memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Người dùng không được phép đặt hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // dia chi
            String address = objReq.getString("address");
            if(address==null || address.equals("")){
                resp1.put("code", 300);
                resp1.put("description", "Vui lòng nhập địa chỉ nhận hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            //
            // cart
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            BookingDAO bookingDAO = new BookingDAO(bookDAO.getConnection());
            JSONObject cartJSON = objReq.getJSONObject("cart");
            Member member = memberLogin.getMember();
            Date bookingDate = new Date();
            ArrayList<Booked> bookeds = new ArrayList<>();
            String bookingNote = "";
            try{bookingNote=cartJSON.getString("cart_note");}catch(Exception e){}
            for(Object bookedJSON : cartJSON.getJSONArray("books")){
                Book book = bookDAO.getBookByID(((JSONObject)bookedJSON).getInt("book_id"));
                if(bookDAO.getBookByID(book.getId())==null){
                    resp1.put("code",300);
                    resp1.put("description","Không được đặt sách không tồn tại");
                    writer.println(resp1.toString());
                    writer.close();
                    bookDAO.close();
                    return;
                }
                // String bookedNote = ((JSONObject)bookedJSON).getString("book_note");
                bookeds.add(new Booked(book, book.getPrice(), ((JSONObject)bookedJSON).getInt("book_number"), null));
            }
            // voucher
            ArrayList<VoucherBooking> voucherBookings = new ArrayList<>();
            try {
                JSONArray vouchersJSON = objReq.getJSONArray("voucher_booking");
                for(Object vJSON : vouchersJSON){
                    if(((JSONObject)vJSON).getInt("voucher_booking_id")<=0){
                        continue;
                    }
                    VoucherBooking v = bookingDAO.getVoucherByID(((JSONObject)vJSON).getInt("voucher_booking_id"));
                    if(v==null || v.getActive()<=0){
                        resp1.put("code",300);
                        resp1.put("description","Voucher "+v.getName()+" Không tồn tại");
                        writer.println(resp1.toString());
                        writer.close();
                        bookingDAO.close();
                        return;
                    }
                    voucherBookings.add(v);
                }
            }catch(Exception e){
                voucherBookings.clear();
            }
            // booking
            JSONObject addressJSON = MapBoxUtil.forwardStringAddressToGeoCoding(address);
            String log = null;
            String lat = null;
            try {
                log = addressJSON.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)+"";
                lat = addressJSON.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1)+"";
            } catch (Exception e) {
                log = null;
                lat = null;
            }
            Booking booking = new Booking(-1, bookingDate, member, bookeds, bookingNote,-1, 0, address, voucherBookings);
            booking.setLong(log);
            booking.setLat(lat);
            // 
            if(!bookingDAO.addBooking(booking)){
                resp1.put("code",300);
                resp1.put("description","Không đặt được sách");
                writer.println(resp1.toString());
                writer.close();
                bookingDAO.close();
                return;
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
            // ExecutorService executorService = Executors.newFixedThreadPool(1);
            // executorService.execute(new Runnable() {
            //     @Override
            //     public void run() {
                    try {
                        Mail.sendMail(memberLogin.getMember().getEmail(),Mail.createrFormBookingSuccess(bookingDAO.getBookingByID(booking.getId())));
                    } catch (Exception e1) {e1.printStackTrace();}
            //         finally{
            //             executorService.shutdown();
            //         }
            //     }
            // });
            // payment
            try {
                int mode = objReq.getJSONObject("payment_type").getInt("mode");
                if(mode==1){// vnpay
                    booking.setMember(member);
                    resp1.put("url_payment", vnPayPayment(booking, null, vnpay.getIpAddress(req)));
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void getAmountBooking(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            if(!memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Người dùng không được phép đặt hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            //
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            BookingDAO bookingDAO = new BookingDAO(bookDAO.getConnection());
            // cart
            JSONObject cartJSON = objReq.getJSONObject("cart");
            Member member = memberLogin.getMember();
            Date bookingDate = new Date();
            ArrayList<Booked> bookeds = new ArrayList<>();
            String bookingNote = "";
            try{bookingNote=cartJSON.getString("cart_note");}catch(Exception e){}
            for(Object bookedJSON : cartJSON.getJSONArray("books")){
                Book book = bookDAO.getBookByID(((JSONObject)bookedJSON).getInt("book_id"));
                if(bookDAO.getBookByID(book.getId())==null){
                    resp1.put("code",300);
                    resp1.put("description","Không được đặt sách không tồn tại");
                    writer.println(resp1.toString());
                    writer.close();
                    bookDAO.close();
                    return;
                }
                // String bookedNote = ((JSONObject)bookedJSON).getString("book_note");
                bookeds.add(new Booked(book, book.getPrice(), ((JSONObject)bookedJSON).getInt("book_number"), null));
            }
            // voucher
            ArrayList<VoucherBooking> voucherBookings = new ArrayList<>();
            Booking booking = new Booking(-1, bookingDate, member, bookeds, bookingNote,-1, 0, null, voucherBookings);
            try {
                JSONArray vouchersJSON = objReq.getJSONArray("voucher_booking");
                for(Object vJSON : vouchersJSON){
                    if(((JSONObject)vJSON).getInt("voucher_booking_id")<=0){
                        continue;
                    }
                    VoucherBooking v = bookingDAO.getVoucherByID(((JSONObject)vJSON).getInt("voucher_booking_id"));
                    if(v==null || v.getActive()<=0){
                        resp1.put("code",300);
                        resp1.put("description","Voucher "+v.getName()+" Không tồn tại");
                        writer.println(resp1.toString());
                        writer.close();
                        bookingDAO.close();
                        return;
                    }
                    voucherBookings.add(v);
                }
            }catch(Exception e){
                voucherBookings.clear();
            }
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("amount", booking.totalPrice());
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void getListPaymentType(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            //
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("payment_type", listPaymentType());
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private JSONArray listPaymentType(){
        JSONArray list = new JSONArray();
        list.put(new JSONObject("{\"mode\":0,\"name\":\"Thanh toán khi nhận hàng\", \"logo\":\"\"}"));
        list.put(new JSONObject("{\"mode\":1,\"name\":\"Ví VNPay\",\"logo\":\"/src/app/assets/images/logovnpay.png\"}"));
        return list;
    }
    private String vnPayPayment(Booking bk, String bankCode1, String ipAddress) throws UnsupportedEncodingException{
        String vnp_TxnRef = bk.getId()+"";// id don hang cua minh
        long amount =(long) bk.totalPrice();
        String orderType = "other";
        String bankCode = bankCode1;
        String vnp_IpAddr = ipAddress;
        String vnp_TmnCode = Init.vnp_TmnCodeSource;//
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        // ====================== tehm du lieu de goi api vnpay
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef+"////////"+bk.getMember().getId());
        vnp_Params.put("vnp_OrderType", orderType);
        // ngon ngu
        // String locate = null;
        // if (locate != null && !locate.isEmpty()) {
        //     vnp_Params.put("vnp_Locale", locate);
        // } else {
        //     vnp_Params.put("vnp_Locale", "vn");
        // }
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", Init.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        // try {
        //     vnp_Params.put("vnp_Bill_Mobile", objReq.getString("vnp_Bill_Mobile"));// vd: 84932224546
        // } catch (Exception e) {
        // }
        try {
            vnp_Params.put("vnp_Bill_Email", bk.getMember().getEmail());
        } catch (Exception e) {
        }
        // try {
        //     vnp_Params.put("vnp_ExpireDate", objReq.getString("vnp_ExpireDate"));// thoi gian het han thanh toan
        //                                                                             // yyyyMMddHHmmss
        // } catch (Exception e) {
        // }
        try {
            // vnp_Params.put("vnp_Bill_FirstName", objReq.getString("vnp_Bill_FirstName"));
            vnp_Params.put("vnp_Bill_LastName", bk.getMember().getFullName());
        } catch (Exception e) {
        }
        try {
            // vnp_Params.put("vnp_Bill_Country", objReq.getString("vnp_Bill_Country"));
            // vnp_Params.put("vnp_Bill_City", objReq.getString("vnp_Bill_City"));
            vnp_Params.put("vnp_Bill_Address", bk.getAddress());

        } catch (Exception e) {
        }
        vnp_Params.put("vnp_Inv_Company", "Cửa hàng sách DATTP");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnpay.hmacSHA512(Init.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Init.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }
    private void getHistoryTransaction(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            //
            PaymentDAO paymentDAO = new PaymentDAO();
            if(!paymentDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // lay du lieu gui den
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.parse(objReq.getString("date_from"));// kiem tra dinh dang ngay gui den
            String dateFrom = null;
            try {
                dateFrom = objReq.getString("date_from") + " 00:00:00";
            } catch (Exception e) {
                dateFrom = format.format(new Date()) + " 00:00:00";
            }
            // thuc thi 
            JSONArray historyTransaction = null;
            if(memberLogin.getMember().getGroup().equals(Init.ADMIN_GROUP)){
                historyTransaction = paymentDAO.getAllHistoryTransaction(dateFrom);
            }else{
                historyTransaction = paymentDAO.getHistoryTransactionByMemberID(memberLogin.getMember().getId(), dateFrom);
            }
            paymentDAO.close();
            // tra ket qua
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("list", historyTransaction);
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void getHistoryTransactionDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            // System.out.println("REQUEST DATA: "+objReq.toString());
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
            // 
            String bookingID = objReq.getString("booking_id");
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(objReq.getString("date"));
            //
            JSONObject data = vnpay.vnpayQuery(bookingID, new SimpleDateFormat("yyyyMMddHHmmss").format(date), vnpay.getIpAddress(req));
            if(!data.getString("vnp_ResponseCode").equals("00")){
                resp1.put("code", 300);
                resp1.put("description", data.getString("vnp_Message"));
                return;
            }
            JSONObject result = new JSONObject();
            result.put("booking_id", data.getString("vnp_TxnRef"));
            result.put("date", convertDate(data.getString("vnp_PayDate")));
            result.put("amount", Long.parseLong(data.getString("vnp_Amount"))/100);
            result.put("free_amount", Long.parseLong(data.getString("vnp_FeeAmount"))/100);
            result.put("card_holder", data.getString("vnp_CardHolder"));
            result.put("card_number", data.getString("vnp_CardNumber"));
            result.put("transaction_no", data.getString("vnp_TransactionNo"));//ma giao dich cua vnpay
            result.put("bank_code", data.getString("vnp_BankCode"));
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

    private void getListBookingStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            resp1.put("code",200);
            resp1.put("description","Thành công");
            resp1.put("list", getStatusBooking());
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
                bookingJSON.put("booking_address",bk.getAddress());
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
            // lay du lieu gui den
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateFrom = objReq.getString("date");
            String dateTo = null;
            format.parse(dateFrom);// kiem tra tinh hop le cua date
            try{dateTo = objReq.getString("date_to");}catch(Exception e){}
            if(dateTo!=null){
                format.parse(dateTo);
            }
            int bookingSuccess = -99;
            try {
                bookingSuccess = objReq.getInt("booking_success");
            } catch (Exception e) {
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
            ArrayList<Booking> bookings = null; 
            if(bookingSuccess<-1 || bookingSuccess>1){// khong co tieu chi loc theo trang thai don hang hoac khong ton tai trang thai duoc yeu cau
                bookings = bookingDAO.getAllByDate(dateFrom,dateTo);
            }else{
                bookings = bookingDAO.getAllByDate(dateFrom,dateTo,bookingSuccess);
            }
            bookingDAO.close();
            for(Booking bk : bookings){
                JSONObject bookingJSON = new JSONObject();
                bookingJSON.put("booking_id",bk.getId());
                bookingJSON.put("booking_date",bk.getDateStrMail());
                bookingJSON.put("booking_note",bk.getNote());
                bookingJSON.put("booking_total_price", bk.totalPrice());
                bookingJSON.put("booking_success", bk.getSuccess());
                bookingJSON.put("booking_pay",bk.getPay());
                bookingJSON.put("booking_address",bk.getAddress());
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
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        if(url.equals(host+"/boooking/payment_return")){
            try {
                paymentReturn(req, resp);
            } catch (NumberFormatException | SQLException | JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }
    private void paymentReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException, NumberFormatException, SQLException, JSONException, ParseException{
        PrintWriter writer = resp.getWriter();
        JSONObject resp1 = new JSONObject();
        Map fields = new HashMap();
        for (Enumeration params = req.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(req.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        String vnp_SecureHash = req.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = vnpay.hashAllFields(fields,Init.vnp_HashSecret);
        if (!signValue.equals(vnp_SecureHash)) {
            resp1.put("code", -200);
            resp1.put("description", "Dữ liệu đã bị sửa đổi");
            writer.println(notificationPayment(resp1));
            writer.close();
            return;
        }
        
        resp1.put("date", convertDate(req.getParameter("vnp_PayDate")));
        resp1.put("amount", req.getParameter("vnp_Amount"));
        resp1.put("booking_id", req.getParameter("vnp_TxnRef"));
        resp1.put("bank_code", req.getParameter("vnp_BankCode"));
        resp1.put("description", req.getParameter("vnp_OrderInfo"));
        try {
            if(req.getParameter("vnp_BankTranNo")==null){
                resp1.put("bank_tran_code", "");
            }else{
                resp1.put("bank_tran_code", req.getParameter("vnp_BankTranNo"));
            }
        } catch (Exception e) {
            resp1.put("bank_tran_code", "");
        }
        try {
            if(req.getParameter("vnp_TransactionNo")==null){
                resp1.put("vnpay_tran_code", "");
            }else{
                resp1.put("vnpay_tran_code", req.getParameter("vnp_TransactionNo"));
            }
        } catch (Exception e) {
            resp1.put("vnpay_tran_code", "");
        }
        // kiem tra giao dich co thanh cong khong
        if(!req.getParameter("vnp_TransactionStatus").equals("00")){
            resp1.put("code", 300);
            resp1.put("description", "Giao dịch không thành công");
            writer.println(notificationPayment(resp1));
            writer.close();
            return;
        }
        // giao dich thanh cong
        BookingDAO bookingDAO = new BookingDAO();
        if(!bookingDAO.connect()){// ket noi voi csdl
            bookingDAO.close();
            resp1.put("code",300);
            resp1.put("description","Thanh toán thành công(Lịch sử giao dịch chưa được lưu)");
            writer.println(notificationPayment(resp1));
            writer.close();
            return;
        }
        // kiem tra xem giao giao dich da duoc luu trong csdl chua
        PaymentDAO PaymentDAO = new PaymentDAO(bookingDAO.getConnection());
        if(PaymentDAO.getTransaction(resp1.getInt("booking_id"), convertDateSql(req.getParameter("vnp_PayDate")))!=null){// da luu roi
            bookingDAO.close();
            resp1.put("code",200);
            resp1.put("description","Thanh toán thành công");
            writer.println(notificationPayment(resp1));
            writer.close();
            return;
        }
        // chua luu giao dich
        // trong vnp_OrderInfo co id khach hang
        int memberID = Integer.parseInt(resp1.getString("description").split("////////")[1]);
        JSONObject data = new JSONObject();
        data.put("member_id", memberID);
        data.put("booking_id", resp1.getInt("booking_id"));
        data.put("date", resp1.getString("date"));
        data.put("amount", resp1.getLong("amount")/100);
        data.put("payment_type", "VNPay");
        if(!PaymentDAO.saveTransaction(data)){// luu giao dich
            PaymentDAO.close();
            resp1.put("code",500);
            resp1.put("description","Thanh toán thành công(Lịch sử giao dịch chưa được lưu)");
            writer.println(notificationPayment(resp1));
            writer.close();
            return;
        }
        // cap nhat lai so tien da thanh toan
        if(!bookingDAO.updatePay(Integer.parseInt(req.getParameter("vnp_TxnRef")), Float.parseFloat(req.getParameter("vnp_Amount"))/100)){
            bookingDAO.close();
            resp1.put("code",500);
            resp1.put("description","Thanh toán thành công(Lịch sử giao dịch chưa được lưu)");
            writer.println(notificationPayment(resp1));
            writer.close();
            return;
        }
        bookingDAO.close();
        resp1.put("code",200);
        resp1.put("description","Thanh toán thành công");
        writer.println(notificationPayment(resp1));
        writer.close();
    }
    private String notificationPayment(JSONObject data){
        if(data.getInt("code")==-200) return
        "<!DOCTYPE html>"
        +"<html lang=\"en\">"
        +"    <head>"
        +"        <meta charset=\"UTF-8\">"
        +"        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
        +"        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        +"        <title>Thanh toán đơn hàng</title>"
        +"        <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">"
        +"        <script src=\"https://kit.fontawesome.com/e7c3a3eb40.js\" crossorigin=\"anonymous\"></script>"
        +"    </head>"
        +"    <body>"
        +"        <div class=\"vh-100 d-flex justify-content-center align-items-center\">"
        +"            <div class=\"col-md-7\">"
        +"                <div class=\"border border-3 border-danger\"></div>"
        +"                <div class=\"card  bg-white shadow p-5\">"
        +"                    <div class=\"mb-4 text-center\">"
        +"                        <i class=\"fa-regular fa-circle-xmark fa-beat\" style=\"color: #f43434;font-size:75px;\"></i>"
        +"                    </div>"
        +"                    <div class=\"text-center\">"
        +"                        <h1>"+data.getString("description")+"</h1>"
        +"                    </div>"
        +"                </div>"
        +"            </div>"
        +"        </div>"
        +"    </body>"
        +"</html>";

        
        if(data.getInt("code")==200) return 
        "<!DOCTYPE html>"
        +"<html lang=\"en\">"
        +"    <head>"
        +"        <meta charset=\"UTF-8\">"
        +"        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
        +"        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        +"        <title>Thanh toán đơn hàng</title>"
        +"        <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">"
        +"        <script src=\"https://kit.fontawesome.com/e7c3a3eb40.js\" crossorigin=\"anonymous\"></script>"
        +"    </head>"
        +"    <body>"
        +"        <div class=\"vh-100 d-flex justify-content-center align-items-center\">"
        +"            <div class=\"col-md-7\">"
        +"                <div class=\"border border-3 border-success\"></div>"
        +"                <div class=\"card  bg-white shadow p-5\">"
        +"                    <div class=\"mb-4 text-center\">"
        +"                        <i class=\"fa-solid fa-circle-check fa-beat\" style=\"color: #36d339;font-size:75px;\"></i>"
        +"                    </div>"
        +"                    <div class=\"text-center\">"
        +"                        <h1>"+data.getString("description")+" !</h1>"
        +"                        <div class=\"col-md-12\" style=\"margin:30px auto;\">"
        +"                            <div class=\"row\">"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã đơn hàng: "+data.getInt("booking_id")+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Tổng tiền: "+Mail.formatNumberMonney(data.getLong("amount")/100)+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Ngày: "+data.getString("date")+"</p>"
        +"                                </div>"
        +"                            </div>"
        +"                            <div class=\"row\">"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã ngân hàng: "+data.getString("bank_code")+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã GD VNPAY: "+data.getString("vnpay_tran_code")+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã GD ngân hàng: "+data.getString("bank_tran_code")+"</p>"
        +"                                </div>"
        +"                            </div>"
        +"                        </div>"
        // +"                        <a class=\"btn btn-outline-success\" href=\"#/dashboard\">Back Home</a>"
        +"                    </div>"
        +"                </div>"
        +"            </div>"
        +"        </div>"
        +"    </body>"
        +"</html>";
        // error
        return 
        "<!DOCTYPE html>"
        +"<html lang=\"en\">"
        +"    <head>"
        +"        <meta charset=\"UTF-8\">"
        +"        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
        +"        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        +"        <title>Thanh toán đơn hàng</title>"
        +"        <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">"
        +"        <script src=\"https://kit.fontawesome.com/e7c3a3eb40.js\" crossorigin=\"anonymous\"></script>"
        +"    </head>"
        +"    <body>"
        +"        <div class=\"vh-100 d-flex justify-content-center align-items-center\">"
        +"            <div class=\"col-md-7\">"
        +"                <div class=\"border border-3 border-danger\"></div>"
        +"                <div class=\"card  bg-white shadow p-5\">"
        +"                    <div class=\"mb-4 text-center\">"
        +"                        <i class=\"fa-regular fa-circle-xmark fa-beat\" style=\"color: #f43434;font-size:75px;\"></i>"
        +"                    </div>"
        +"                    <div class=\"text-center\">"
        +"                        <h1>"+data.getString("description")+" !</h1>"
        +"                        <div class=\"col-md-12\" style=\"margin:30px auto;\">"
        +"                            <div class=\"row\">"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã đơn hàng: "+data.getInt("booking_id")+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Tổng tiền: "+Mail.formatNumberMonney(data.getLong("amount"))+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Ngày: "+data.getString("date")+"</p>"
        +"                                </div>"
        +"                            </div>"
        +"                            <div class=\"row\">"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã ngân hàng: "+data.getString("bank_code")+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã GD VNPAY: "+data.getString("vnpay_tran_code")+"</p>"
        +"                                </div>"
        +"                                <div class=\"col-md-4\" style=\"text-align:left;\">"
        +"                                    <p>Mã GD ngân hàng: "+data.getString("bank_tran_code")+"</p>"
        +"                                </div>"
        +"                            </div>"
        +"                        </div>"
        // +"                        <a class=\"btn btn-outline-success\" href=\"#/dashboard\">Back Home</a>"
        +"                    </div>"
        +"                </div>"
        +"            </div>"
        +"        </div>"
        +"    </body>"
        +"</html>";
    }
    private String convertDate(String date){
        return date.substring(8, 10)+":"+date.substring(10, 12)+":"+date.substring(12, 14)+" "
        +date.substring(6, 8)+"/"+date.substring(4, 6)+"/"+date.substring(0, 4);
    }
    private String convertDateSql(String date){
        return date.substring(0, 4)+"-"+date.substring(4, 6)+"-"+date.substring(6, 8)
        +" "+date.substring(8, 10)+":"+date.substring(10, 12)+":"+date.substring(12, 14);
    }
}
