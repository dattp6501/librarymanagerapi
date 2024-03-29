package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Book;
import model.Booked;
import model.Booking;
import model.Commemt;
import model.Member;
import model.Type;

public class BookDAO extends DAO{
    public BookDAO(){super();}

    public ArrayList<Book> getBooks(String title, int limit, Type typeBook) throws SQLException{
        if(typeBook == null){
            return getBooksByTitle(title, limit);
        }
        return getBooksByTitleAndType(title, limit, typeBook);
    }

    public ArrayList<Book> getBooksByTitle(String title, int limit) throws SQLException{
        ArrayList<Book> list = new ArrayList<>();
        String select = "select * from books where title like ? "
        +" ORDER BY create_date DESC";
        if(limit>0){
            select+= " limit ? ";
        }
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setString(1, "%"+title+"%");
        if(limit>0){
            ps.setInt(2, limit);
        }
        ResultSet res = ps.executeQuery();
        while(res.next()){
            Book book = new Book();
            book.setId(res.getInt("id"));
            book.setTitle(res.getString("title"));
            book.setAuthor(res.getString("author"));
            book.setType(res.getString("typeb"));
            try {
                book.setReleaseDate(res.getString("release_date"));
                book.setCreateDate(res.getString("create_date"));
            }catch(ParseException e) {e.printStackTrace();}
            book.setPageNumber(res.getInt("page_number"));
            try {
                book.setImageBytes(res.getBytes("image"));
            } catch (SQLException e) {
                book.setImageBytes(null);
            }
            book.setDescription(res.getString("description"));
            book.setPrice(res.getFloat("price"));
            book.setNumber(res.getInt("number"));
            book.setTypeo(getTypeByBookID(res.getInt("id")));
            list.add(book);
        }
        res.close();
        return list;
    }

    public ArrayList<Book> getBooksByTitleAndType(String title, int limit, Type typeBook) throws SQLException{
        ArrayList<Book> list = new ArrayList<>();
        String select = "select * from books where title like ? "
        +"ORDER BY create_date DESC";
        if(limit>0){
            select+= "limit ? ";
        }
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setString(1, "%"+title+"%");
        if(limit>0){
            ps.setInt(2, limit);
        }
        ResultSet res = ps.executeQuery();
        while(res.next()){
            Book book = new Book();
            book.setId(res.getInt("id"));
            book.setTitle(res.getString("title"));
            book.setAuthor(res.getString("author"));
            book.setType(res.getString("typeb"));
            try {
                book.setReleaseDate(res.getString("release_date"));
                book.setCreateDate(res.getString("create_date"));
            }catch(ParseException e) {e.printStackTrace();}
            book.setPageNumber(res.getInt("page_number"));
            try {
                book.setImageBytes(res.getBytes("image"));
            } catch (SQLException e) {
                book.setImageBytes(null);
            }
            book.setDescription(res.getString("description"));
            book.setPrice(res.getFloat("price"));
            book.setNumber(res.getInt("number"));
            book.setTypeo(getTypeByBookID(res.getInt("id")));
            list.add(book);
        }
        res.close();
        for(Book b : list){
            System.out.println(b.getTypeo());
        }
        list.removeIf(b->(b.getTypeo()!=null&&(b.getTypeo().getId()!=typeBook.getId())));
        return list;
    }

    public Type getTypeByBookID(int bookID) throws SQLException{
        Type type = null;
        String select = "SELECT * FROM type_of_book WHERE book_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, bookID);
        ResultSet res = ps.executeQuery();
        if(res.next()){
            select = "SELECT * FROM type_book WHERE id=?";
            ps = connection.prepareStatement(select);
            ps.setInt(1, res.getInt("type_id")); 
            res = ps.executeQuery();
            if(res.next()){
                type = new Type();
                type.setId(res.getInt("id"));
                type.setName(res.getString("name"));
                type.setNote(res.getString("note"));
            }
        }
        return type;
    }

    public boolean addTypeForBook(Book book) throws SQLException{
        if(book.getTypeo()==null){
            return true;
        }
        boolean ok = false;
        String insert = "INSERT INTO type_of_book(book_id,type_id) VALUES(?,?)";
        PreparedStatement ps = connection.prepareStatement(insert);
        ps.setInt(1,book.getId());
        ps.setInt(2, book.getTypeo().getId());
        ok = ps.executeUpdate()>0;
        return ok;
    }

    public boolean deleteTypeForBook(Book book) throws SQLException{
        if(getTypeByBookID(book.getId())==null){
            return true;
        }
        String delete = "DELETE FROM type_of_book WHERE book_id=?";
        PreparedStatement ps = connection.prepareStatement(delete);
        ps.setInt(1,book.getId());
        return ps.executeUpdate()>0;
    }

    public boolean updateTypeForBook(Book book) throws SQLException{
        if(book.getTypeo()==null){
            if(getTypeByBookID(book.getId()) == null){// sach luc chua update chua co the loai
                return true;
            }
            return deleteTypeForBook(book);
        }
        if(getTypeByBookID(book.getId())==null){// sach luc chua cap nhat chua co the loai
            return addTypeForBook(book);
        }
        if(getTypeByBookID(book.getId()).getId() == book.getTypeo().getId()){// the loai cu giong the loai moi
            return true;
        }
        String update = "UPDATE type_of_book SET type_id=? WHERE book_id=?";
        PreparedStatement ps = connection.prepareStatement(update);
        ps.setInt(1, book.getTypeo().getId());
        ps.setInt(2,book.getId());
        return ps.executeUpdate()>0;
    }

    public Book getBookByID(int bookID) throws SQLException{
        String select = "SELECT * FROM books WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, bookID);
        ResultSet res = ps.executeQuery();
        Book book = null;
        if(res.next()){
            book = new Book();
            book.setId(res.getInt("id"));
            book.setTitle(res.getString("title"));
            book.setAuthor(res.getString("author"));
            book.setType(res.getString("typeb"));
            try {
                book.setReleaseDate(res.getString("release_date"));
                book.setCreateDate(res.getString("create_date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            book.setPageNumber(res.getInt("page_number"));
            try {
                book.setImageBytes(res.getBytes("image"));
            } catch (SQLException e) {
                book.setImageBytes(null);
            }
            book.setDescription(res.getString("description"));
            book.setNumber(res.getInt("number"));
            book.setPrice(res.getFloat("price"));
            book.setTypeo(getTypeByBookID(bookID));
        }
        return book;
    }
    public boolean add(Book book) throws SQLException{
        connection.setAutoCommit(false);
        boolean ok = false;
        String sql = "insert into books(title,author,typeb,release_date,page_number,image,description,price,number,create_date) values(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getType());
        ps.setString(4, book.getReleaseDateFormat());
        ps.setInt(5, book.getPageNumber());
        ps.setBytes(6, book.getImageBytes());
        ps.setString(7, book.getDescription());
        ps.setFloat(8, book.getPrice());
        ps.setInt(9, book.getNumber());
        ps.setString(10, book.getCreateDateFormat());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            book.setId(res.getInt(1));
            if(updateTypeForBook(book)){
                ok = true;
            }
        }
        if(ok){
            connection.setAutoCommit(true);
        }else{
            connection.rollback();
        }
        return ok;
    }
    public boolean update(Book book) throws SQLException{
        connection.setAutoCommit(false);
        boolean ok = false;
        String sql = "update books set title=?,author=?,typeb=?,release_date=?,page_number=?,image=?,description=?,price=?,number=? where id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getType());
        ps.setString(4, book.getReleaseDateFormat());
        ps.setInt(5, book.getPageNumber());
        ps.setBytes(6, book.getImageBytes());
        ps.setString(7, book.getDescription());
        ps.setFloat(8,book.getPrice());
        ps.setInt(9,book.getNumber());
        ps.setInt(10, book.getId());
        ok = ps.executeUpdate()>=1;
        ok = ok&&updateTypeForBook(book);
        if(ok){
            connection.setAutoCommit(true);
        }else{
            connection.rollback();
        }
        return ok;
    }
    //------------------delete--------------------
    public boolean delete(Book book) throws SQLException{
        connection.setAutoCommit(false);
        boolean ok = false;
        String delete = "delete from books where id=? ";
        PreparedStatement ps = connection.prepareStatement(delete);
        ps.setInt(1, book.getId());
        ok = ps.executeUpdate()>0;
        delete = "DELETE FROM comment WHERE book_id=?";
        ps = connection.prepareStatement(delete);
        ps.setInt(1, book.getId());
        ok = ok&&ps.executeUpdate()>=0;
        ok = ok&&deleteTypeForBook(book);
        if(ok){
            connection.setAutoCommit(true);
        }else{
            connection.rollback();
        }
        return ok;
    }
    // comment
    public boolean addComment(Book book, Commemt comment) throws SQLException{
        boolean ok = false;
        String insert = "INSERT INTO comment(book_id,member_id,star,content,date) VALUES(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(insert);
        ps.setInt(1, book.getId());
        ps.setInt(2, comment.getMember().getId());
        ps.setInt(3, comment.getStar());
        ps.setString(4, comment.getContent());
        ps.setString(5, comment.getDateStr());
        ok = ps.executeUpdate()>0;
        return ok;
    }
    public void getComments(Book book) throws SQLException, ParseException{
        ArrayList<Commemt> commemts = new ArrayList<>();
        String select = "SELECT * FROM comment WHERE book_id=? ORDER BY date DESC";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, book.getId());
        ResultSet res = ps.executeQuery();
        while(res.next()){
            MemberDAO memberDAO = new MemberDAO(); memberDAO.setConnection(connection);
            Member member = memberDAO.getMemberByID(res.getInt("member_id"));
            int star = res.getInt("star");
            String content = res.getString("content");
            String dateStr = res.getString("date");
            commemts.add(new Commemt(member, content, star, dateStr));
        }
        book.setComments(commemts);
    }

    public boolean addBooked(Booking booking) throws SQLException{
        boolean ok = false;
        String insert = "INSERT INTO booked(booking_id,book_id,price,number,note) VALUES(?,?,?,?,?)";
        PreparedStatement ps = null;
        for(Booked booked : booking.getBookeds()){
            ps = connection.prepareStatement(insert);
            ps.setInt(1, booking.getId());
            ps.setInt(2, booked.getBook().getId());
            ps.setFloat(3, booked.getPrice());
            ps.setFloat(4, booked.getNumber());
            ps.setString(5, booked.getNote());
            ok = ps.executeUpdate()>0;
            // xoa sach trong gio hang
            CartDAO cartDAO = new CartDAO(); cartDAO.setConnection(connection);
            ok = ok&&cartDAO.removeBookInCart(booked.getBook(), booking.getMember().getId());
            // 
            // ps.close();
            // String update = "UPDATE books SET number=number-? WHERE id=?";
            // ps = connection.prepareStatement(update);
            // ps.setInt(1, booked.getNumber());
            // ps.setInt(2, booked.getBook().getId());
            // ok = ps.executeUpdate()>0;
            // ps.close();
            if(ok==false){
                break;
            }
        }
        return ok;
    }
    public ArrayList<Booked> getBookedByBookingID(int bookingID) throws SQLException{
        ArrayList<Booked> list = new ArrayList<>();
        String select = "SELECT * FROM booked WHERE booking_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, bookingID);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            Booked booked = new Booked();
            Book book = getBookByID(res.getInt("book_id"));
            booked.setBook(book);
            booked.setNumber(res.getInt("number"));
            booked.setPrice(res.getFloat("price"));
            booked.setNote(res.getString("note"));
            list.add(booked);
        }
        return list;
    }

    public boolean addType(Type type) throws SQLException{
        boolean ok = false;
        String insert = "INSERT INTO type_book(name,note) VALUES(?,?)";
        PreparedStatement ps = connection.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, type.getName());
        ps.setString(2, type.getNote());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            type.setId(res.getInt(1));
            ok = true;
        }
        return ok;
    }

    public Type getTypeByID(int typeID) throws SQLException{
        Type type = null;
        String select = "SELECT * FROM type_book WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, typeID);
        ResultSet res = ps.executeQuery();
        if(res.next()){
            type = new Type();
            type.setId(typeID);
            type.setName(res.getString("name"));
            type.setNote(res.getString("note"));
        }
        return type;
    }

    public ArrayList<Type> getAllType() throws SQLException{
        ArrayList<Type> list = new ArrayList<>();
        String select = "SELECT * FROM type_book";
        PreparedStatement ps = connection.prepareStatement(select);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            Type type = new Type();
            type.setId(res.getInt("id"));
            type.setName(res.getString("name"));
            type.setNote(res.getString("note"));
            list.add(type);
        }
        return list;
    }
    // lay danh sách các quyển sách đã được mua trong 1 khoản thòi gian, chưa có lượng sao đánh giá trung bình
    public JSONArray getListBookedDateRange(Date from, Date to) throws SQLException{
        JSONArray list = new JSONArray();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String sql = "SELECT book_id, sum(number) as number "
        + "FROM booking "
        + "INNER JOIN booked "
        + "ON booking.id  = booked.booking_id "
        + "WHERE ?<=date AND date<=?"
        + "GROUP BY(book_id) "
        + "ORDER BY number DESC ";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, format.format(from)+" 00:00:00");
        ps.setString(2, format.format(to)+" 23:59:59");
        ResultSet res = ps.executeQuery();
        while(res.next()){
            Book book = getBookByID(res.getInt("book_id"));
            if(book!=null){
                JSONObject bookJSON = new JSONObject();
                bookJSON.put("book_id", res.getInt("book_id"));
                bookJSON.put("book_number", res.getInt("number"));
                bookJSON.put("book_title", book.getTitle());
                bookJSON.put("book_author", book.getAuthor());
                // bookJSON.put("book_image", book.getImage());
                if(book.getTypeo()!=null){
                    JSONObject typeJSON = new JSONObject();
                    typeJSON.put("type_id", book.getTypeo().getId());
                    typeJSON.put("type_name", book.getTypeo().getName());
                    typeJSON.put("type_note", book.getTypeo().getNote());
                    bookJSON.put("book_typeo", typeJSON);
                }
                list.put(bookJSON);
            }
        }
        return list;
    }

    // lay danh sách các quyển sách đã được mua trong 1 khoảng thòi gian, đã có lượng dánh giá sao trung bình
    public JSONArray getListBookedDateRange1(Date from, Date to) throws SQLException{
        JSONArray list = new JSONArray();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String sql = "SELECT booktmp.book_id,booktmp.number,sum(comment.star)/count(comment.star) as star "
        + "FROM (SELECT book_id, sum(number) as number "
        + "FROM booking "
        + "INNER JOIN booked "
        + "ON booking.id = booked.booking_id "
        + "WHERE ?<=date AND date<=? "
        + "GROUP BY(book_id)) as booktmp "
        + "LEFT JOIN comment "
        + "ON booktmp.book_id = comment.book_id "
        + "GROUP BY(booktmp.book_id) "
        + "ORDER BY number DESC ";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, format.format(from)+" 00:00:00");
        ps.setString(2, format.format(to)+" 23:59:59");
        ResultSet res = ps.executeQuery();
        while(res.next()){
            Book book = getBookByID(res.getInt("book_id"));
            
            if(book!=null){
                JSONObject bookJSON = new JSONObject();
                bookJSON.put("book_id", res.getInt("book_id"));
                bookJSON.put("book_number", res.getInt("number"));
                bookJSON.put("book_star", res.getFloat("star"));
                bookJSON.put("book_title", book.getTitle());
                bookJSON.put("book_author", book.getAuthor());
                bookJSON.put("book_image", book.getImage());
                // bookJSON.put("book_image", book.getImage());
                if(book.getTypeo()!=null){
                    JSONObject typeJSON = new JSONObject();
                    typeJSON.put("type_id", book.getTypeo().getId());
                    typeJSON.put("type_name", book.getTypeo().getName());
                    typeJSON.put("type_note", book.getTypeo().getNote());
                    bookJSON.put("book_typeo", typeJSON);
                }
                list.put(bookJSON);
            }
        }
        return list;
    }



    public static void main(String[] args) {
        BookDAO dao = new BookDAO();
        if(!dao.connect()){
            System.out.println("Khong ket noi duoc CSDL");
            return;
        }
        Book book = new Book();book.setId(24); book.setCreateDate(new Date()); book.setTitle("test");
        Type type = new Type(); type.setId(1);
        book.setTypeo(null);
        try {
            Date from = null;
            Date to = null;
            Calendar calendarFrom = Calendar.getInstance();
            calendarFrom.set(Calendar.YEAR, 2022);
            calendarFrom.set(Calendar.MONTH, 0);
            calendarFrom.set(Calendar.DATE, 1);
            from = calendarFrom.getTime();

            Calendar calendarTo = Calendar.getInstance();
            int maxDateOfMonth = calendarTo.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendarTo.set(Calendar.MONTH, 11);
            calendarTo.set(Calendar.DATE, maxDateOfMonth);
            to = calendarTo.getTime();
            for(Object o : dao.getListBookedDateRange1(from, to)){
                try {
                    System.out.println(((JSONObject) o).getFloat("book_star"));
                } catch (Exception e) {
                    System.out.println(0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dao.close(); 
    }
}