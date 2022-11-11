package api;

import java.io.IOException;
import java.io.PrintWriter;
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
import filters.SessionFilter;
import global.Init;
import model.Book;
import utils.JsonCustom;

@WebServlet(urlPatterns = {"/book/get_books","/book/update_books","/book/delete_books","/book/add_books"})
public class BookAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        if(url.equals(host+"/book/get_books")){
            getBooks(req,resp);
        }else if(url.equals(host+"/book/update_books")){
            updateBooks(req,resp);
        }else if(url.equals(host+"/book/delete_books")){
            deleteBooks(req,resp);
        }else if(url.equals(host+"/book/add_books")){
            addBooks(req,resp);
        }
    }
    //
    private void getBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String title = objReq.getString("title");
            int limit = -1;
            try {
                limit = objReq.getInt("limit");
            } catch (Exception e) {
            }
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",500);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject result = new JSONObject();
            ArrayList<Book> list = bookDAO.getBooksByTitle(title, limit);
            bookDAO.close();
            JSONArray listJson = new JSONArray();
            for(Book b : list){
                JSONObject bJson = new JSONObject();
                bJson.put("id", b.getId());
                bJson.put("title", b.getTitle());
                bJson.put("author", b.getAuthor());
                bJson.put("type", b.getType());
                bJson.put("release_date", b.getReleaseDateFormat());
                bJson.put("page_number", b.getPageNumber());
                bJson.put("image", b.getImage());
                listJson.put(bJson);
            }
            result.put("list", listJson);
            result.put("total", list.size());
            resp1.put("code",200);
            resp1.put("description", "Thành công");
            resp1.put("result", result);
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void updateBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String session = objReq.getString("session");
            int res = SessionFilter.checkSession(session);
            if(res==0){
                resp1.put("code", 700);
                resp1.put("description", "Người dùng chưa đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(res==2){
                resp1.put("code", 700);
                resp1.put("description", "Hết phiên đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject bookJson = objReq.getJSONObject("book");
            int id = bookJson.getInt("id");
            String title = bookJson.getString("title");
            String author = bookJson.getString("author");
            String type = bookJson.getString("type");
            String releaseDate = bookJson.getString("release_date");
            int pageNumber = bookJson.getInt("page_number");
            String image = bookJson.getString("image");
            if(image.equals("")){
                resp1.put("code",300);
                resp1.put("description","Không có ảnh");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Book book = new Book(id, pageNumber, title, author, type, releaseDate, image);
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",500);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookDAO.update(book)){
                resp1.put("code",300);
                resp1.put("description", "Sách không tồn tại");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            bookDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void deleteBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String session = objReq.getString("session");
            int res = SessionFilter.checkSession(session);
            if(res==0){
                resp1.put("code", 700);
                resp1.put("description", "Người dùng chưa đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(res==2){
                resp1.put("code", 700);
                resp1.put("description", "Hết phiên đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject bookJson = objReq.getJSONObject("book");
            int id = bookJson.getInt("id");
            Book book = new Book(id, -1, null, null, null, new Date(), "");
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",500);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookDAO.delete(book)){
                resp1.put("code",300);
                resp1.put("description", "sách không tồn tại");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            resp1.put("code",200);
            resp1.put("description", "Thành công");
            bookDAO.close();
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void addBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        PrintWriter writer = resp.getWriter();
        JSONObject objReq = JsonCustom.toJsonObject(req.getReader());
        JSONObject resp1 = new JSONObject();
        try {
            System.out.println("REQUEST DATA: "+objReq.toString());
            String session = objReq.getString("session");
            int res = SessionFilter.checkSession(session);
            if(res==0){
                resp1.put("code", 700);
                resp1.put("description", "Người dùng chưa đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(res==2){
                resp1.put("code", 700);
                resp1.put("description", "Hết phiên đăng nhập");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            JSONObject bookJson = objReq.getJSONObject("book");
            String title = bookJson.getString("title");
            String author = bookJson.getString("author");
            String type = bookJson.getString("type");
            String releaseDate = bookJson.getString("release_date");
            int pageNumber = bookJson.getInt("page_number");
            String image = bookJson.getString("image");
            if(image.equals("")){
                resp1.put("code",300);
                resp1.put("description","Không có ảnh");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Book book = new Book(-1, pageNumber, title, author, type, releaseDate, image);
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",500);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookDAO.add(book)){
                resp1.put("code",300);
                resp1.put("description", "Không thêm được sách");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            bookDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }
}