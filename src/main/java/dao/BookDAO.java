package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;

import model.Book;

public class BookDAO extends DAO{
    public BookDAO(){super();}

    public ArrayList<Book> getBooksByTitle(String title, int limit) throws SQLException{
        ArrayList<Book> list = new ArrayList<>();
        String select = "select * from books where title like ? ";
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
            } catch (ParseException e) {
                e.printStackTrace();
            }
            book.setPageNumber(res.getInt("page_number"));
            list.add(book);
        }
        res.close();
        return list;
    }
    public boolean add(Book book) throws SQLException{
        boolean ok = false;
        String sql = "insert into books(title,author,typeb,release_date,page_number) values(?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getType());
        ps.setString(4, book.getReleaseDateFormat());
        ps.setInt(5, book.getPageNumber());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            book.setId(res.getInt(1));
            ok = true;
        }
        res.close();
        return ok;
    }
    public boolean update(Book book) throws SQLException{
        boolean ok = false;
        String sql = "update books set title=?,author=?,typeb=?,release_date=?,page_number=? where id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getType());
        ps.setString(4, book.getReleaseDateFormat());
        ps.setInt(5, book.getPageNumber());
        ps.setInt(6, book.getId());
        ok = ps.executeUpdate()>=1;
        return ok;
    }
    //------------------delete--------------------
    public boolean delete(Book book) throws SQLException{
        boolean ok = false;
        String delete = "delete from books where id=? ";
        PreparedStatement ps = connection.prepareStatement(delete);
        ps.setInt(1, book.getId());
        ok = ps.executeUpdate()>0;
        return ok;
    }
}
