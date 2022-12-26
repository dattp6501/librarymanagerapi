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
import model.Commemt;
import model.MemberLogin;
import model.Type;
import utils.JsonCustom;

@WebServlet(urlPatterns = {"/book/*","/book/get_books","/book/update_books","/book/delete_books","/book/add_books","/book/add_comment","/book/get_comments"})
public class BookAPI extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String url = req.getRequestURI();
        String host = Init.HOST;
        System.out.println(url);
        if(url.equals(host+"/book/get_books")){
            getBooks(req,resp);
        }else if(url.equals(host+"/book/update_books")){
            updateBooks(req,resp);
        }else if(url.equals(host+"/book/delete_books")){
            deleteBooks(req,resp);
        }else if(url.equals(host+"/book/add_books")){
            addBooks(req,resp);
        }else if(url.equals(host+"/book/add_comment")){
            addComment(req, resp);
        }else if(url.equals(host+"/book/get_comments")){
            getComments(req, resp);
        }else if(url.equals(host+"/book/get_book_by_id")){
            getBookByID(req, resp);
        }else if(url.equals(host+"/book/get_all_type")){
            getAllType(req, resp);
        }else if(url.equals(host+"/book/add_type")){
            addType(req, resp);
        }
    }
    //
    private void getBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            
            String title = objReq.getString("title");
            int limit = -1;
            try{limit = objReq.getInt("limit");}catch(Exception e) {}
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
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
                bJson.put("book_id", b.getId());
                bJson.put("book_title", b.getTitle());
                bJson.put("book_author", b.getAuthor());
                bJson.put("book_type", b.getType());
                bJson.put("book_release_date", b.getReleaseDateFormat());
                bJson.put("book_create_date", b.getCreateDateFormat());
                bJson.put("book_page_number", b.getPageNumber());
                bJson.put("book_image", b.getImage());
                bJson.put("book_price", b.getPrice());
                bJson.put("book_number", b.getNumber());
                bJson.put("book_description", b.getDescription());
                // khiem tra xem co pahi moi tao khong
                long time = new Date().getTime()  - 7*24*60*60*1000; // 1 tuan
                if(b.getCreateDate().getTime()>=time){
                    bJson.put("book_is_new", true);
                }else{
                    bJson.put("book_is_new", false);
                }
                // type
                if(b.getTypeo()!=null){
                    JSONObject typeJSON = new JSONObject();
                    typeJSON.put("type_id", b.getTypeo().getId());
                    typeJSON.put("type_name", b.getTypeo().getName());
                    typeJSON.put("type_note", b.getTypeo().getNote());
                    bJson.put("book_typeo", typeJSON);
                }
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

    private void getBookByID(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            int bookID = objReq.getInt("book_id");
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            Book b = bookDAO.getBookByID(bookID);
            bookDAO.close();
            JSONObject result = new JSONObject();
            JSONObject bJson = new JSONObject();
            bJson.put("book_id", b.getId());
            bJson.put("book_title", b.getTitle());
            bJson.put("book_author", b.getAuthor());
            bJson.put("book_type", b.getType());
            bJson.put("book_release_date", b.getReleaseDateFormat());
            bJson.put("book_create_date", b.getCreateDateFormat());
            bJson.put("book_page_number", b.getPageNumber());
            bJson.put("book_image", b.getImage());
            bJson.put("book_price", b.getPrice());
            bJson.put("book_number", b.getNumber());
            bJson.put("book_description", b.getDescription());
            // khiem tra xem co pahi moi tao khong
            long time = new Date().getTime()  - 7*24*60*60*1000; // 1 tuan truoc
            if(b.getCreateDate().getTime()>=time){
                bJson.put("book_is_new", true);
            }else{
                bJson.put("book_is_new", false);
            }
            // type
            if(b.getTypeo()!=null){
                JSONObject typeJSON = new JSONObject();
                typeJSON.put("type_id", b.getTypeo().getId());
                typeJSON.put("type_name", b.getTypeo().getName());
                typeJSON.put("type_note", b.getTypeo().getNote());
                bJson.put("book_typeo", typeJSON);
            }
            result.put("book", bJson);

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

            JSONObject bookJson = objReq.getJSONObject("book");
            int id = bookJson.getInt("book_id");
            String title = bookJson.getString("book_title");
            String author = bookJson.getString("book_author");
            String releaseDate = bookJson.getString("book_release_date");
            String type = null;
            int pageNumber = -1;
            String image = null;
            String description = null;
            float price = 0;
            int number = 0;
            Date createDate = null;// chua su dung den
            Type typeo = null;
            try{type = bookJson.getString("book_type");}catch(Exception e){}
            try{
                pageNumber = bookJson.getInt("book_page_number");
                if(pageNumber<=0){
                    resp1.put("code",300);
                    resp1.put("description","Số trang phải > 0");
                    writer.println(resp1.toString());
                    writer.close();
                    return;
                }
            }catch(Exception e){}
            try{image = bookJson.getString("book_image");}catch(Exception e){}
            try{description = bookJson.getString("book_description");}catch(Exception e){}
            try{
                price = bookJson.getFloat("book_price");
                if(price<=0){
                    resp1.put("code",300);
                    resp1.put("description","Giá sách phải > 0");
                    writer.println(resp1.toString());
                    writer.close();
                    return;
                }
            }catch(Exception e){}
            try{
                number = bookJson.getInt("book_number");
                if(price<=0){
                    resp1.put("code",300);
                    resp1.put("description","Số lượng sách phải > 0");
                    writer.println(resp1.toString());
                    writer.close();
                    return;
                }
            }catch(Exception e){}
            try{
                JSONObject typeOJSON = bookJson.getJSONObject("book_typeo");
                if(typeOJSON.getInt("type_id")>0){
                    typeo = new Type(typeOJSON.getInt("type_id"), null, null);
                }
            }catch(Exception e){
                typeo = null;
            }
            Book book = new Book(id, pageNumber, title, author, type, releaseDate, image, description,price,number,createDate,typeo);
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
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
            String message = e.getMessage().toLowerCase();
            if(message.indexOf("duplicate")>=0){
                if(message.indexOf("'title'")>=0 || message.indexOf("'books.title_unique'")>=0){
                    message = "Tiêu đề truyện đã tồn tại";
                }
            }
            resp1.put("description",message);
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

            JSONObject bookJson = objReq.getJSONObject("book");
            int id = bookJson.getInt("book_id");
            Book book = new Book(); book.setId(id);
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
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
            
            JSONObject bookJson = objReq.getJSONObject("book");
            String title = bookJson.getString("book_title");
            String author = bookJson.getString("book_author");
            String releaseDate = bookJson.getString("book_release_date");
            String type = null;
            int pageNumber = -1;
            String image = null;
            String description = null;
            float price = 0;
            int number = 0;
            Date createDate = new Date();
            Type typeo = null;
            try{type = bookJson.getString("book_type");}catch(Exception e){}
            try{
                pageNumber = bookJson.getInt("book_page_number");
                if(pageNumber<=0){
                    resp1.put("code",300);
                    resp1.put("description","Số trang phải > 0");
                    writer.println(resp1.toString());
                    writer.close();
                    return;
                }
            }catch(Exception e){}
            try{image = bookJson.getString("book_image");}catch(Exception e){}
            try{description = bookJson.getString("book_description");}catch(Exception e){}
            try{
                price = bookJson.getFloat("book_price");
                if(price<=0){
                    resp1.put("code",300);
                    resp1.put("description","Giá sách phải >= 0");
                    writer.println(resp1.toString());
                    writer.close();
                    return;
                }
            }catch(Exception e){}
            try{
                number = bookJson.getInt("book_number");
                if(number<=0){
                    resp1.put("code",300);
                    resp1.put("description","Số lượng sách phải >= 0");
                    writer.println(resp1.toString());
                    writer.close();
                    return;
                }
            }catch(Exception e){}
            try{
                JSONObject typeOJSON = bookJson.getJSONObject("book_typeo");
                if(typeOJSON.getInt("type_id")>0){
                    typeo = new Type(typeOJSON.getInt("type_id"), null, null);
                }
            }catch(Exception e){
                typeo = null;
            }
            Book book = new Book(-1, pageNumber, title, author, type, releaseDate, image, description,price,number,createDate,typeo);
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
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
            String message = e.getMessage().toLowerCase();
            if(message.indexOf("duplicate")>=0){
                if(message.indexOf("'title'")>=0 || message.indexOf("'books.title_unique'")>=0){
                    message = "Tiêu đề truyện đã tồn tại";
                }
            }
            resp1.put("description",message);
        }
        writer.println(resp1.toString());
        writer.close();
    }
    // comment
    private void addComment(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            // book
            JSONObject bookJSON = objReq.getJSONObject("book");
            int bookID = bookJSON.getInt("book_id");
            Book book = new Book();
            book.setId(bookID);
            // comment
            String content = "";
            int star = objReq.getInt("star");
            try{content = objReq.getString("content");}catch(Exception e){}
            Commemt commemt = new Commemt(memberLogin.getMember(), content, star, new Date());
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookDAO.addComment(book, commemt)){
                resp1.put("code",300);
                resp1.put("description", "Không thêm được đánh giá");
                return;
            }
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }
    private void getComments(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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
            // book
            JSONObject bookJSON = objReq.getJSONObject("book");
            int bookID = bookJSON.getInt("book_id");
            Book book = new Book();
            book.setId(bookID);
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            bookDAO.getComments(book);
            JSONObject result = new JSONObject();
            JSONArray listJSON = new JSONArray();
            for(Commemt commemt : book.getCommemts()){
                JSONObject commentJSON = new JSONObject();
                commentJSON.put("comment_star",commemt.getStar());
                commentJSON.put("comment_content",commemt.getContent());
                commentJSON.put("comment_date",commemt.getDateStr());
                JSONObject memberJSON = new JSONObject();
                memberJSON.put("member_id", commemt.getMember().getId());
                memberJSON.put("member_fullname", commemt.getMember().getFullName());
                memberJSON.put("member_email", commemt.getMember().getEmail());
                memberJSON.put("member_username", commemt.getMember().getUserName());
                memberJSON.put("member_avatar", commemt.getMember().getImage());
                commentJSON.put("member", memberJSON);
                listJSON.put(commentJSON);
            }
            result.put("total",listJSON.length());
            result.put("book_id",bookID);
            result.put("list", listJSON);
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

    private void getAllType(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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

            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            ArrayList<Type> list = bookDAO.getAllType(); bookDAO.close();
            JSONObject result = new JSONObject();
            JSONArray listJSON = new JSONArray();
            for(Type t : list){
                JSONObject typeJSON = new JSONObject();
                typeJSON.put("type_id",t.getId());
                typeJSON.put("type_name",t.getName());
                typeJSON.put("type_note",t.getNote());
                listJSON.put(typeJSON);
            }
            result.put("total",listJSON.length());
            result.put("list", listJSON);
            resp1.put("code",200);
            resp1.put("description", "Thành công");
            resp1.put("result",result);
        } catch (Exception e) {
            resp1.put("code",300);
            resp1.put("description",e.getMessage());
        }
        writer.println(resp1.toString());
        writer.close();
    }

    private void addType(HttpServletRequest req, HttpServletResponse resp) throws IOException{
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

            Type type = new Type();
            type.setName(objReq.getJSONObject("type").getString("type_name"));
            try{
                type.setNote(objReq.getJSONObject("type").getString("type_note"));
            }catch(Exception e){}
            BookDAO bookDAO = new BookDAO();
            if(!bookDAO.connect()){
                resp1.put("code",400);
                resp1.put("description","Không kết nối được CSDL");
                writer.println(resp1.toString());
                writer.close();
                return;
            }
            if(!bookDAO.addType(type)){
                resp1.put("code",400);
                resp1.put("description","Không thêm được thể loại mới");
                writer.println(resp1.toString());
                writer.close();
                bookDAO.close();
                return;
            }
            bookDAO.close();
            resp1.put("code",200);
            resp1.put("description", "Thành công");
        } catch (Exception e) {
            resp1.put("code",300);
            String message = e.getMessage().toLowerCase();
            if(message.indexOf("duplicate")>=0){
                if(message.indexOf("'name'")>=0 || message.indexOf("'type_book.name'")>=0){
                    message = "Thể loại đã tồn tại";
                }
            }
            resp1.put("description",message);
        }
        writer.println(resp1.toString());
        writer.close();
    }
}