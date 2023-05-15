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
    private float pay=0;
    private String address;
    private String log;
    private String lat;
    private ArrayList<VoucherBooking> voucherBookings;
    public Booking() {
    }
    public Booking(int id, Date date, Member member, ArrayList<Booked> bookeds, String note, int success, float pay, String address, ArrayList<VoucherBooking> voucherBookings) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.bookeds = bookeds;
        this.note = note;
        this.voucherBookings = voucherBookings;
        this.success = success;
        this.pay = pay;
        this.address = address;
    }
    public String getLong() {
        return log;
    }
    public void setLong(String log) {
        this.log = log;
    }
    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
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
    public String getDateStrMail() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
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
    public float getPay() {
        return pay;
    }
    public void setPay(float pay) {
        this.pay = pay;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
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
        return "Booking [id=" + id + ", date=" + date +", pay="+ pay+", address="+ address + ", member=" + member + ", bookeds=" + bookeds + ", note=" + note
                + ", success=" + success + ", voucherBookings=" + voucherBookings + "]";
    }
}