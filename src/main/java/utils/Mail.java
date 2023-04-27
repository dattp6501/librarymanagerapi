package utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import model.Booked;
import model.Booking;
import model.VoucherBooking;

public class Mail {
    public final static String USERNAME = "kiennc.b19at101@stu.ptit.edu.vn";
    public final static String PASSWORD = "3Au5Re9U";
    public final static String MAIL = "kiennc.b19at101@stu.ptit.edu.vn";
    // public final static String USERNAME = "truongphucdat6501@gmail.com";
    // public final static String PASSWORD = "Passdat652001@";
    // public final static String MAIL = "truongphucdat6501@gmail.com";

    public static void sendMail(String mailTo,String content) throws AddressException, MessagingException {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.office365.com");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
        message.setSubject("CỬA HÀNG SÁCH DATTP");
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(content, "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);
        Transport.send(message);
    }
    public static String createrForm1(String message){
        String html = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "\n"
                + "<head>\n"
                + "</head>\n"
                + "\n"
                + "<body>\n"
                + "    <h3 style=\"color: blue;\">Cảm ơn bạn đã quan tâm tới chúng tôi</h3>\n"
                + "    <div>"+message+"</div\n>"
                + "    <div>Full Name :Trương Phúc Đạt</div>\n"
                + "    <div>Phone : 0375361858</div>\n"
                + "    <div>address : Hồng Châu, Đông Hưng, Thái Bình</div>\n"
                + "    <h3 style=\"color: blue;\">Thank you very much!</h3>\n"
                + "\n"
                + "</body>\n"
                + "\n"
                + "</html>";
        return html;
    }
    public static String createrFormBookingSuccess(Booking booking){
        String style = 
        "<style>"
        +"table {"
        +"  font-family: arial, sans-serif;"
        +"  border-collapse: collapse;"
        +"  width: 100%;"
        +"}"
        +"td, th {"
        +"  border: 1px solid #dddddd;"
        +"  text-align: left;"
        +"  padding: 8px;"
        +"}"
        +"tr:nth-child(even) {"
        +"  background-color: #dddddd;"
        +"};"
        +"</style>";
        String html = 
        "<!DOCTYPE html>\n"
        + "<html lang=\"en\">\n"
        + "\n"
        + "<head>\n"
        + style
        + "</head>\n"
        + "\n"
        + "<body>\n"
        + "    <h3 style=\"color: blue;\">ĐẶT HÀNG THÀNH CÔNG</h3>\n"
        + "    <div>Khách hàng : "+booking.getMember().getFullName()+"</div>\n"
        + "    <div>Ngày đặt : "+booking.getDateStrMail()+"</div>\n"
        + "    <div>Địa chỉ nhận hàng : "+booking.getAddress()+"</div>\n";
        // danh sach san pham
        String table = 
            "<table>"
            +"<tr>"
            +"  <th>Tên sách</th>"
            +"  <th>SL x Giá</th>"
            +"  <th>Tổng</th>"
            +"</tr>";
        for(Booked b : booking.getBookeds()){
            String tr =
            "<tr>"
            +"  <td>"+b.getBook().getTitle()+"</td>"
            +"  <td>"+b.getNumber()+" x "+formatNumberMonney(b.getPrice())+"</td>"
            +"  <td>"+formatNumberMonney(b.getPrice()*b.getNumber())+"</td>"
            +"</tr>";
            table += tr;
        }
        // voucher
        String vouchers = 
        "<tr>"
        +"  <td colspan=\"2\">"+"Voucher"+"</td>"
        +"  <td>";
        for(VoucherBooking v : booking.getVoucherBookings()){
            if(v.getType().equals("n")){
                vouchers += "-"+formatNumberMonney(v.getValue())+"<br/>";
            }else if(v.getType().equals("%")){
                vouchers += "-"+formatNumber(v.getValue())+"%"+"<br/>";
            }
        }
        vouchers = vouchers.substring(0, vouchers.length()-5);
        vouchers+="</td>";
        vouchers+="</tr>";
        table += vouchers;
        // thanh tien
        table += "  <td colspan=\"2\">"+"Thành tiền"+"</td>";
        table += "  <td>"+formatNumberMonney(booking.totalPrice())+"</td>";
        // 
        table+="</table>";
        html += table;
        // 
        html += "    <h3 style=\"color: blue;\">Cảm ơn bạn đã quan tâm tới chúng tôi</h3>\n"
        + "\n"
        + "</body>\n"
        + "\n"
        + "</html>";


        
        return html;
    }
    public static String formatNumberMonney(float number){
        // return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(number);
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(number)+ " " 
        + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).getCurrency().toString();
    }
    public static String formatNumber(float number){
        // return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(number);
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(number);
    }

    public static void main(String[] args) throws AddressException, MessagingException {
        // sendMail( "truongphucdat6501@gmail.com", Mail.createrForm1("Đặt hàng thành công"));
        System.out.println(formatNumber(10000.01f));
    }
}