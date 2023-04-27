package utils;

import java.util.Date;

import org.json.JSONObject;

public class vnpay {
    private static String urlPay = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    +"?vnp_Amount=1000"// so tien
    +"&vnp_Command=pay"
    +"&vnp_CreateDate="+new Date().getTime()
    +"&vnp_CurrCode=VND"
    +"&vnp_IpAddr=127.0.0.1"
    +"&vnp_Locale=vn"
    +"&vnp_OrderInfo=Thanh+toan+don+hang+%3A5"
    +"&vnp_OrderType=other"
    +"&vnp_ReturnUrl=https%3A%2F%2Fdomainmerchant.vn%2FReturnUrl"//URL thông báo kết quả giao dịch khi Khách hàng kết thúc thanh toán
    +"&vnp_TmnCode=DEMOV210"// Mã website của merchant trên hệ thống của VNPAY
    +"&vnp_TxnRef=1"// ma don hang
    +"&vnp_Version=2.1.0"
    +"&vnp_SecureHash=3e0d61a0c0534b2e36680b3f7277743e8784cc4e1d68fa7d276e79c23be7d6318d338b477910a27992f5057bb1582bd44bd82ae8009ffaf6d141219218625c42";//Mã kiểm tra (checksum) để đảm bảo dữ liệu của giao dịch không bị thay đổi trong quá trình chuyển từ merchant sang VNPAY. Việc tạo ra mã này phụ thuộc vào cấu hình của merchant và phiên bản api sử dụng. Phiên bản hiện tại hỗ trợ SHA256, HMACSHA512.


    public static void main(String[] args) {
        System.out.println(urlPay);
    }
}
