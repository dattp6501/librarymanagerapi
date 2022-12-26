package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Booking {
    private int id;
    private Date date;
    private Member member;
    private ArrayList<Booked> bookeds;
    private String note;
    private int success;//-1: dang cho xu ly, 0: da duoc xu ly nhung bi huy, 1: tao thanh cong
    private ArrayList<VoucherBooking> voucherBookings;
    public Booking() {
    }
    public Booking(int id, Date date, Member member, ArrayList<Booked> bookeds, String note, int success, ArrayList<VoucherBooking> voucherBookings) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.bookeds = bookeds;
        this.note = note;
        this.voucherBookings = voucherBookings;
        this.success = success;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public Date getDate() {
        return date;
    }
    public String getDateStr() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setDate(String dateStr) throws ParseException {
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
    }
    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }
    public ArrayList<Booked> getBookeds() {
        return bookeds;
    }
    public void setBookeds(ArrayList<Booked> bookeds) {
        this.bookeds = bookeds;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public int getSuccess() {
        return success;
    }
    public void setSuccess(int success) {
        this.success = success;
    }
    public ArrayList<VoucherBooking> getVoucherBookings() {
        return voucherBookings;
    }
    public void setVoucherBookings(ArrayList<VoucherBooking> voucherBookings) {
        this.voucherBookings = voucherBookings;
    }
    public float totalPrice(){
        float totalPrice  = 0;
        for(Booked bed : bookeds){
            totalPrice += bed.getPrice()*bed.getNumber();
        }
        if(voucherBookings!=null && voucherBookings.size()>0){
            VoucherBooking voucher = voucherBookings.get(0);
            if(voucher.getType().equals("%")){// tinh theo phan tram
                totalPrice = totalPrice - totalPrice*voucher.getValue()/100;
            }else if(voucher.getType().equals("n")){
                totalPrice -= voucher.getValue();
            }
        }
        if(totalPrice<0){
            totalPrice = 0;
        }
        return totalPrice; 
    }
    @Override
    public String toString() {
        return "Booking [id=" + id + ", date=" + date + ", member=" + member + ", bookeds=" + bookeds + ", note=" + note
                + ", success=" + success + ", voucherBookings=" + voucherBookings + "]";
    }
}