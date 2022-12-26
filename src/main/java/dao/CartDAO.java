package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.Book;
import model.Cart;
import model.Member;

public class CartDAO extends DAO{
    public CartDAO() {
    }
    public Cart getCartByMemberID(int memberID) throws SQLException{
        Member member = new Member();
        member.setId(memberID);
        MemberDAO memberDAO = new MemberDAO(); memberDAO.setConnection(connection);
        if(!memberDAO.get(member)){// ID thanh vien khong ton tai
            return null;
        }
        Cart cart = new Cart(member,new ArrayList<Book>());
        String select = "SELECT * FROM cart WHERE member_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, memberID);
        ResultSet res = ps.executeQuery();
        while(res.next()){
            BookDAO bookDAO = new BookDAO(); bookDAO.setConnection(connection);
            Book book = bookDAO.getBookByID(res.getInt("book_id"));
            if(book!=null){
                book.setNumber(res.getInt("book_number"));
            }else{
                book = new Book();
                book.setId(res.getInt("book_id"));
                book.setNumber(res.getInt("book_number"));
            }
            cart.getBooks().add(book);
        }
        ps.close();
        return cart;
    }
    public boolean addBookToCard(Book newBook, int memberID) throws SQLException{
        Cart cart = getCartByMemberID(memberID);
        if(cart == null){// ID thanh vien khong ton tai
            return false;
        }
        boolean ok = false;
        PreparedStatement ps = null;
        String update = "UPDATE cart SET book_number=book_number+? WHERE member_id=? AND book_id=?";
        String insert = "INSERT INTO cart(member_id,book_id,book_number) VALUES(?,?,?)";
        if(cart.getBooks().contains(newBook)){// da co trong gio hang
            ps = connection.prepareStatement(update);
            ps.setInt(1, newBook.getNumber());
            ps.setInt(2, memberID);
            ps.setInt(3, newBook.getId());
        }else{// chua co trong gio hang
            ps = connection.prepareStatement(insert);
            ps.setInt(1, memberID);
            ps.setInt(2, newBook.getId());
            ps.setInt(3, newBook.getNumber());
        }
        ok = ps.executeUpdate()>0;
        ps.close();
        return ok;
    }

    public boolean removeBookInCart(Book book, int memberID) throws SQLException{
        boolean ok = false;
        String delete = "DELETE FROM cart WHERE member_id=? AND book_id=?";
        PreparedStatement ps = connection.prepareStatement(delete);
        ps.setInt(1, memberID);
        ps.setInt(2, book.getId());
        ok = ps.executeUpdate()>0;
        ps.close();
        return ok;
    }
    public static void main(String[] args) {
        CartDAO dao = new CartDAO();
        if(!dao.connect()){
            System.out.println("Khong ket noi duoc CSDL");
            return;
        }
        Book book = new Book(); book.setId(1);
        try {
            System.out.println(dao.removeBookInCart(book,6));
            dao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
