package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.Book;
import model.Cart;
import model.Member;

public class CartDAO extends DAO{
    public CartDAO() {
    }
    public CartDAO(Connection connection) {
        super(connection);
    }
    public boolean addCartForMember(int memberID) throws SQLException{
        String insert = "INSERT INTO cart(member_id) VALUES(?)";
        PreparedStatement ps = connection.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, memberID);
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        boolean ok = res.next();
        ps.close();
        return ok;
    }
    public Cart getCartByMemberID(int memberID) throws SQLException{
        Member member = new Member();
        member.setId(memberID);
        MemberDAO memberDAO = new MemberDAO(); memberDAO.setConnection(connection);
        if(!memberDAO.get(member)){// ID thanh vien khong ton tai
            return null;
        }
        // lay id cart cua member
        Cart cart = new Cart(member,new ArrayList<Book>(),-1);
        String select = "SELECT * FROM cart WHERE member_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, memberID);
        ResultSet res = ps.executeQuery();
        if(!res.next()){
            ps.close();
            return null;
        }
        cart.setId(res.getInt("id"));
        res.close();
        // lay san pham trong cart
        select = "SELECT * FROM product_of_cart WHERE cart_id=?";
        ps = connection.prepareStatement(select);
        ps.setInt(1, cart.getId());
        res = ps.executeQuery();
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
        // lay gio hang cua member
        Cart cart = getCartByMemberID(memberID);
        // them san pham vao gio hang
        if(cart.getBooks().contains(newBook)){// da co trong gio hang
            // String update = "UPDATE product_of_cart SET book_number=book_number+? WHERE cart_id=? AND book_id=?";
            // ps = connection.prepareStatement(update);
            // ps.setInt(1, newBook.getNumber());
            // ps.setInt(2, cart.getId());
            // ps.setInt(3, newBook.getId());
            return true;
        }
        // chua co trong gio hang
        boolean ok = false;
        PreparedStatement ps = null;
        String insert = "INSERT INTO product_of_cart(cart_id,book_id,book_number) VALUES(?,?,?)";
        ps = connection.prepareStatement(insert);
        ps.setInt(1, cart.getId());
        ps.setInt(2, newBook.getId());
        ps.setInt(3, newBook.getNumber());
        ok = ps.executeUpdate()>0;
        ps.close();
        return ok;
    }

    public boolean removeBookInCart(Book book, int memberID) throws SQLException{
        // lay id cua cart
        String select = "SELECT * FROM cart WHERE member_id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, memberID);
        ResultSet res = ps.executeQuery();
        if(!res.next()){
            ps.close();
            return false;
        }
        int cartID = res.getInt("id");
        res.close();
        ps.close();
        // xoa san pham khoi gio hang
        boolean ok = false;
        String delete = "DELETE FROM product_of_cart WHERE cart_id=? AND book_id=?";
        ps = connection.prepareStatement(delete);
        ps.setInt(1, cartID);
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
        try {
            int[] list = {1,5,6,7,8,17,20,21};
            for(int i=0; i<list.length; i++){
                System.out.println(dao.addCartForMember(list[i]));
            }
            dao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
