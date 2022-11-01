package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.Member;

public class MemberDAO extends DAO{
    public MemberDAO(){super();}

    public boolean checkLogin(Member member) throws SQLException{
        boolean ok = false;
        String select = "select * "
            + "from members "
            + "where username=? and passwd=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setString(1, member.getUserName());
        ps.setString(2, member.getPassWord());
        ResultSet res = ps.executeQuery();
        if(res.next()){
            member.setId(res.getInt("id"));
            member.setFullName(res.getString("fullname"));
            member.setEmail(res.getString("email"));
            ok = true;
        }
        res.close();
        return ok;
    }

    public boolean add(Member member) throws SQLException{
        boolean ok = false;
        String sql = "insert into members(fullname,email,username,passwd) values(?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        ps.setString(1,member.getFullName());
        ps.setString(2,member.getEmail());
        ps.setString(3,member.getUserName());
        ps.setString(4,member.getPassWord());
        ps.executeUpdate();
        ResultSet res = ps.getGeneratedKeys();
        if(res.next()){
            member.setId(res.getInt(1));
            ok = true;
        }
        res.close();
        return ok;
    }
}
