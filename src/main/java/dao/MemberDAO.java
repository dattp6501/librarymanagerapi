package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.Group;
import model.Member;
import model.MemberLogin;

public class MemberDAO extends DAO{
    public MemberDAO(){super();}
    
    public boolean addMemberLogin(MemberLogin memL) throws SQLException{
        String sql = "INSERT INTO member_login(session_id,member_id,time_to) VALUES(?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, memL.getSession());
        ps.setInt(2, memL.getMember().getId());
        ps.setString(3, memL.getTimeStr());
        return ps.executeUpdate()>0;
    }
    public boolean DeleteMemberLoginBySession(MemberLogin memL) throws SQLException{
        String sql = "DELETE FROM member_login WHERE binary session_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, memL.getSession());
        return ps.executeUpdate()>0;
    } 
    public MemberLogin getMemberLoginBySession(String session) throws SQLException, ParseException{
        String sql = "SELECT * FROM member_login WHERE binary session_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, session);
        ResultSet res = ps.executeQuery();
        if(!res.next()){
            res.close();
            ps.close();
            return null;
        }
        MemberLogin memL = new MemberLogin();
        Date dateTimeTo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        .parse(res.getString("time_to"));
        Member member = getMemberByID(res.getInt("member_id"));

        memL.setSession(session);
        memL.setTime(dateTimeTo);
        memL.setMember(member);

        res.close();
        ps.close();
        return memL;
    }

    public boolean checkLogin(Member member) throws SQLException{
        boolean ok = false;
        String select = "select * "
            + "from members "
            + "where binary username=? and binary passwd=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setString(1, member.getUserName());
        ps.setString(2, member.getPassWord());
        ResultSet res = ps.executeQuery();
        if(res.next()){// log in thanh cong
            member.setId(res.getInt("id"));
            // member.setFullName(res.getString("fullname"));
            // member.setEmail(res.getString("email"));
            member.getGroup().setId(res.getInt("group_id"));
            // check group
            GroupDAO groupDAO = new GroupDAO(); groupDAO.setConnection(connection);
            if(groupDAO.get(member.getGroup())){
                ok = true;
            }
        }
        res.close();
        return ok;
    }

    public boolean add(Member member) throws SQLException{
        boolean ok = false;
        String sql = "insert into members(fullname,email,username,passwd,group_id,address) values(?,?,?,?,?,?)";
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,member.getFullName());
        ps.setString(2,member.getEmail());
        ps.setString(3,member.getUserName());
        ps.setString(4,member.getPassWord());
        ps.setInt(5, member.getGroup().getId());
        ps.setString(6, member.getAddress());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            member.setId(res.getInt(1));
            // tao gio hang cho member
            CartDAO cartDAO = new CartDAO(connection);
            if(cartDAO.addCartForMember(member.getId())){
                ok = true;
                connection.setAutoCommit(true);
            }else{
                connection.rollback();
            }
        }
        res.close();
        ps.close();
        return ok;
    }

    public Member getMemberByID(int memberID) throws SQLException{
        Member member = null;
        String sql = "SELECT * FROM members WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, memberID);
        ResultSet res = ps.executeQuery();
        if(res.next()){
            member = new Member();
            member.setId(memberID);
            member.setFullName(res.getString("fullname"));
            member.setEmail(res.getString("email"));
            member.setUserName(res.getString("username"));
            member.setPassWord(res.getString("passwd"));
            member.setImageBytes(res.getBytes("image"));
            member.setAddress(res.getString("address"));
            // check group
            GroupDAO groupDAO = new GroupDAO(); groupDAO.setConnection(connection);
            Group group = groupDAO.getGroupByID(res.getInt("group_id"));
            member.setGroup(group);
        }
        return member;
    }

    public boolean get(Member member) throws SQLException{
        boolean ok = false;
        String sql = "SELECT * FROM members WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, member.getId());
        ResultSet res = ps.executeQuery();
        if(res.next()){
            member.setFullName(res.getString("fullname"));
            member.setEmail(res.getString("email"));
            member.setUserName(res.getString("username"));
            member.setPassWord(res.getString("passwd"));
            member.setImageBytes(res.getBytes("image"));
            member.setAddress(res.getString("address"));
            ok = true;
        }
        ps.close();
        return ok;
    }

    public boolean update(Member member) throws SQLException{
        boolean ok = false;
        String sql = "UPDATE members SET fullname=?,email=?,username=?,image=?,address=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,member.getFullName());
        ps.setString(2,member.getEmail());
        ps.setString(3,member.getUserName());
        ps.setBytes(4, member.getImageBytes());
        ps.setString(5, member.getAddress());
        ps.setInt(6, member.getId());
        ok = ps.executeUpdate()>0;
        return ok;
    }

    public static void main(String[] args) {
        MemberDAO dao = new MemberDAO();
        if(!dao.connect()){
            System.out.println("Khong ket noi duoc CSDL");
            return;
        }
        try {
            System.out.println(dao.getMemberByID(1));
            dao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}