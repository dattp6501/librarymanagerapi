package api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.BookDAO;
import dao.CartDAO;
import filters.SessionFilter;
import global.Init;
import model.Book;
import model.Cart;
import model.MemberLogin;
import utils.JsonCustom;


@WebServlet(urlPatterns = {"/cart/*"})
public class CartAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        // custemer
        if(url.equals(host+"/cart/get_cart")){
            getCard(req,resp);
        }else if(url.equals(host+"/cart/add_book")){
            addBookToCard(req,resp);
        }else if(url.equals(host+"/cart/add_books")){
            addBooksToCard(req,resp);
        }else if(url.equals(host+"/cart/remove_book")){
            removeBookInCard(req,resp);
        }
    }
    private void getCard(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            if(!memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Người dùng không có chức năng giỏ hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }

            CartDAO cartDAO = new CartDAO();
            if(!cartDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Cart cart = cartDAO.getCartByMemberID(memberLogin.getMember().getId()); cartDAO.close();
            JSONObject result = new JSONObject();
            JSONObject cartJSON = new JSONObject();
            JSONArray listBookJSON = new JSONArray();
            for(Book b : cart.getBooks()){
                JSONObject bookJSON = new JSONObject();
                bookJSON.put("book_id", b.getId());
                bookJSON.put("book_number", b.getNumber());
                try {
                    bookJSON.put("book_title", b.getTitle());
                    bookJSON.put("book_author", b.getAuthor());
                    bookJSON.put("book_type", b.getType());
                    bookJSON.put("book_release_date", b.getReleaseDateFormat());
                    bookJSON.put("book_page_number", b.getPageNumber());
                    bookJSON.put("book_image", b.getImage());
                    bookJSON.put("book_price", b.getPrice());
                    bookJSON.put("book_description", b.getDescription());
                    bookJSON.put("book_note", "");
                }catch(Exception e){}
                listBookJSON.put(bookJSON);
            }
            cartJSON.put("books",listBookJSON);
            result.put("cart", cartJSON);

            resp1.put("code",200);
            resp1.put("description", "Thành công");
            resp1.put("result",result);
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void addBookToCard(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            if(!memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Người dùng không có chức năng giỏ hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }

            JSONObject bookJSON = objReq.getJSONObject("book");
            int bookID = bookJSON.getInt("book_id");
            int bookNumber = bookJSON.getInt("book_number");
            if(bookNumber<=0){
                resp1.put("code",300);
                resp1.put("description","Số lượng sách phải lớn hơn 0");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Book bookDB = bookDAO.getBookByID(bookID);
            if(bookDB==null){
                resp1.put("code",300);
                resp1.put("description","Sách không tồn tại");
                bookDAO.close();
                return;
            }

            CartDAO cartDAO = new CartDAO(); cartDAO.setConnection(bookDAO.getConnection());
            Book book = new Book(); book.setId(bookID); book.setNumber(bookNumber);
            if(!cartDAO.addBookToCard(book, memberLogin.getMember().getId())){
                resp1.put("code",300);
                resp1.put("description", "Không thêm sách vào giỏ hàng được");
                bookDAO.close();
                return;
            }
            bookDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void addBooksToCard(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        /*
        {
            "session": "",
            "books": [
                {
                    "book_id": 10,
                    "book_number": 1
                }
            ]
        }
        */
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
            if(!memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Người dùng không có chức năng giỏ hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            // 
            JSONArray books = objReq.getJSONArray("books");
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            for(int i=0; i<books.length(); i++){
                JSONObject b = (JSONObject)books.get(i);
                int bookNumber = b.getInt("book_number");
                if(bookNumber<=0){// xoa nhung sach co so luong <=0
                    books.remove(i);
                    continue;
                }
                int bookID = b.getInt("book_id");
                Book book = bookDAO.getBookByID(bookID);
                if(book==null){// xoa sach khong co trong cua hang
                    books.remove(i);
                    continue;
                }
            }
            // luu vao db
            CartDAO cartDAO = new CartDAO(); cartDAO.setConnection(bookDAO.getConnection());
            if(!cartDAO.addBooksToCard(books, memberLogin.getMember().getId())){
                resp1.put("code",300);
                resp1.put("description", "Không thêm sách vào giỏ hàng được");
                bookDAO.close();
                return;
            }
            bookDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void removeBookInCard(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            if(!memberLogin.getMember().getGroup().equals(Init.CUSTOMER_GROUP)){
                resp1.put("code", 300);
                resp1.put("description", "Người dùng không có chức năng giỏ hàng");
                writer.println(resp1.toString());
                writer.close();
                return;
            }

            JSONObject bookJSON = objReq.getJSONObject("book");
            int bookID = bookJSON.getInt("book_id");
            Book book = new Book(); book.setId(bookID); 
            CartDAO cartDAO = new CartDAO();
            if(!cartDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!cartDAO.removeBookInCart(book, memberLogin.getMember().getId())){
                resp1.put("code",300);
                resp1.put("description", "Không bỏ sách ra khỏi giỏ hàng được");
                cartDAO.close();
                return;
            }
            cartDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage().replace('"', '\''));
        }
        writer.println(resp1.toString());
        writer.close();
    }
}
