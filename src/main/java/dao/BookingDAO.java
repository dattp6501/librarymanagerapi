package dao;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import model.Booked;
import model.Booking;
import model.Member;
import model.VoucherBooking;

public class BookingDAO extends DAO{
    public BookingDAO() {
    }
    public boolean addBooking(Booking booking) throws SQLException{
        connection.setAutoCommit(false);
        boolean ok = false;
        String insert = "INSERT INTO booking(member_id,date,note,total_price,success) VALUES(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, booking.getMember().getId());
        ps.setString(2, booking.getDateStr());
        ps.setString(3, booking.getNote());
        ps.setFloat(4, booking.totalPrice());
        ps.setInt(5,booking.getSuccess());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            booking.setId(res.getInt(1));
            // luu danh sach sach duoc mua
            BookDAO bookDAO = new BookDAO();
            bookDAO.setConnection(connection);
            if(bookDAO.addBooked(booking)){
                ok = true;
            }
        }
        ok = ok&&addVoucherToBooking(booking);
        if(ok){
            connection.setAutoCommit(true);
        }else{
            connection.rollback();
        }
        return ok;
    }

    public boolean confirmBooking(Booking bk) throws SQLException{
        connection.setAutoCommit(false);
        boolean ok = false;
        String update = "UPDATE books SET number=number-? WHERE id=?";
        PreparedStatement ps = null;
        ps = connection.prepareStatement(update);
        for(Booked booked : bk.getBookeds()){
            ps.setInt(1, booked.getNumber());
            ps.setInt(2, booked.getBook().getId());
            ok = ps.executeUpdate()>0;
            if(!ok){
                break;
            }
        }
        ps.close();
        ok = ok&&updateStatusBooking(bk.getId(), 1);
        if(ok){
            connection.setAutoCommit(true);
        }else{
            connection.rollback();
        }
        return ok;
    }

    public boolean updateStatusBooking(int bookingID,int mode) throws SQLException{
        boolean ok = false;
        String update = "UPDATE booking SET success=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(update);
        ps.setInt(1, mode);
        ps.setInt(2, bookingID);
        ok = ps.executeUpdate()>0;
        return ok;
    }

    public ArrayList<Booking> getByMemberID(int memberID) throws SQLException, ParseException{
        ArrayList<Booking> list = new ArrayList<>();
        String select = "SELECT * FROM booking WHERE member_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, memberID);
        ResultSet res = ps.executeQuery();
        BookDAO bookDAO = new BookDAO(); bookDAO.setConnection(connection);
        while(res.next()){
            Booking booking = new Booking();
            booking.setId(res.getInt("id"));
            booking.setDate(res.getString("date"));
            booking.setNote(res.getString("note"));
            booking.setSuccess(res.getInt("success"));
            Member mem = new Member();
            mem.setId(memberID);
            booking.setMember(mem);
            booking.setBookeds(bookDAO.getBookedByBookingID(booking.getId()));

            booking.setVoucherBookings(getVoucherByBookingID(booking.getId()));
            list.add(booking);
        }
        return list;
    }

    public Booking getBookingByID(int bookingID) throws SQLException, ParseException{
        Booking booking = null;
        String select = "SELECT * FROM booking WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, bookingID);
        ResultSet res = ps.executeQuery();
        BookDAO bookDAO = new BookDAO(); bookDAO.setConnection(connection);
        if(res.next()){
            booking = new Booking();
            booking.setId(bookingID);
            booking.setDate(res.getString("date"));
            booking.setNote(res.getString("note"));
            booking.setSuccess(res.getInt("success"));

            int memberID = res.getInt("member_id");
            Member mem = new Member();
            mem.setId(memberID);
            booking.setMember(mem);

            booking.setBookeds(bookDAO.getBookedByBookingID(booking.getId()));

            booking.setVoucherBookings(getVoucherByBookingID(bookingID));
        }
        return booking;
    }

    public ArrayList<Booking> getAllByDate(String startDate) throws SQLException, ParseException{
        ArrayList<Booking> list = new ArrayList<>();
        String select = "SELECT * FROM booking WHERE date>=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setString(1, startDate);
        ResultSet res = ps.executeQuery();
        BookDAO bookDAO = new BookDAO(); bookDAO.setConnection(connection);
        while(res.next()){
            Booking booking = new Booking();
            booking.setId(res.getInt("id"));
            booking.setDate(res.getString("date"));
            booking.setNote(res.getString("note"));
            booking.setSuccess(res.getInt("success"));
            //member
            Member mem = new Member();
            mem.setId(res.getInt("member_id"));
            MemberDAO memberDAO = new MemberDAO(); memberDAO.setConnection(connection);
            if(!memberDAO.get(mem)){// khach hang khong ton tai
                continue;
            }
            booking.setMember(mem);
            booking.setBookeds(bookDAO.getBookedByBookingID(booking.getId()));

            booking.setVoucherBookings(getVoucherByBookingID(booking.getId()));
            list.add(booking);
        }
        return list;
    }

    public boolean addVoucher(VoucherBooking voucher) throws SQLException{
        boolean ok = false;
        String insert = "INSERT INTO voucher_booking(name,type,value,note,active,created_date) VALUES(?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, voucher.getName());
        ps.setString(2, voucher.getType());
        ps.setFloat(3,voucher.getValue());
        ps.setString(4,voucher.getNote());
        ps.setInt(5,voucher.getActive());
        ps.setString(6,voucher.getCreatedDateStr());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            voucher.setId(res.getInt(1));
            ok = true;
        }
        return ok;
    }

    public boolean updateVoucher(){
        boolean ok = false;



        return ok;
    }

    public boolean setActiveVoucher(VoucherBooking voucher) throws SQLException{
        boolean ok = false;
        String update = "UPDATE voucher_booking SET active=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(update);
        ps.setInt(1,voucher.getActive());
        ps.setInt(2,voucher.getId());
        ok = ps.executeUpdate()>0;
        return ok;
    }

    public VoucherBooking getVoucherByID(int voucherID) throws SQLException{
        VoucherBooking voucher = null;
        String select = "SELECT * FROM voucher_booking WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, voucherID);
        ResultSet res = ps.executeQuery();
        if(res.next()){
            voucher = new VoucherBooking();
            voucher.setId(voucherID);
            voucher.setName(res.getString("name"));
            voucher.setType(res.getString("type"));
            voucher.setValue(res.getFloat("value"));
            voucher.setNote(res.getString("note"));
            voucher.setActive(res.getInt("active"));
            try{voucher.setCreatedDate(res.getString("created_date"));
            }catch(ParseException e) {}
        }
        return voucher;
    }

    public ArrayList<VoucherBooking> getAllVoucher() throws SQLException{
        ArrayList<VoucherBooking> list = new ArrayList<>();
        String select = "SELECT * FROM voucher_booking";
        PreparedStatement ps = connection.prepareStatement(select);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            VoucherBooking voucher  = new VoucherBooking();
            voucher.setId(res.getInt("id"));
            voucher.setName(res.getString("name"));
            voucher.setType(res.getString("type"));
            voucher.setValue(res.getFloat("value"));
            voucher.setNote(res.getString("note"));
            voucher.setActive(res.getInt("active"));
            try{voucher.setCreatedDate(res.getString("created_date"));
            }catch(ParseException e){}
            list.add(voucher);
        }
        return list;
    }

    public ArrayList<VoucherBooking> getVoucherActive() throws SQLException{
        ArrayList<VoucherBooking> list = new ArrayList<>();
        String select = "SELECT * FROM voucher_booking WHERE active=1";
        PreparedStatement ps = connection.prepareStatement(select);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            VoucherBooking voucher  = new VoucherBooking();
            voucher.setId(res.getInt("id"));
            voucher.setName(res.getString("name"));
            voucher.setType(res.getString("type"));
            voucher.setValue(res.getFloat("value"));
            voucher.setNote(res.getString("note"));
            voucher.setActive(res.getInt("active"));
            try{voucher.setCreatedDate(res.getString("created_date"));
            }catch(ParseException e){}
            list.add(voucher);
        }
        return list;
    }

    public ArrayList<VoucherBooking> getVoucherByBookingID(int bookingID) throws SQLException{
        ArrayList<VoucherBooking> list = new ArrayList<>();
        String select = "SELECT * FROM voucher_of_booking WHERE booking_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, bookingID);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            list.add(getVoucherByID(res.getInt("voucher_booking_id")));
        }
        return list;
    }

    public boolean addVoucherToBooking(Booking bk) throws SQLException{
        if(bk.getVoucherBookings()==null || bk.getVoucherBookings().size()<=0){
            return true;
        }
        connection.setAutoCommit(false);
        boolean ok = false;
        String insert = "INSERT INTO voucher_of_booking(booking_id,voucher_booking_id) VALUES(?,?)";
        PreparedStatement ps = connection.prepareStatement(insert);
        ps.setInt(1,bk.getId());
        for(VoucherBooking v : bk.getVoucherBookings()){
            ps.setInt(2,v.getId());
            ok = ps.executeUpdate()>0;
            if(!ok){
                break;
            }
        }
        if(ok){
            connection.setAutoCommit(true);
        }else{
            connection.rollback();
        }
        return ok;
    }




    public static void main(String[] args) {
        BookingDAO dao = new BookingDAO();
        if(!dao.connect()){
            System.out.println("khong ket noi duoc CSDL");
            return;
        }
        try {
            System.out.println(dao.getBookingByID(1).totalPrice());
            dao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
