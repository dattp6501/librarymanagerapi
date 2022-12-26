package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Group;


public class GroupDAO extends DAO{
    public GroupDAO() {
    }

    public boolean get(Group group) throws SQLException{
        boolean ok = false;
        String select = "SELECT * FROM group_ WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, group.getId());
        ResultSet res = ps.executeQuery();
        if(res.next()){
            group.setName(res.getString("group_name"));
            group.setNote(res.getString("note"));
            ok = true;
        }
        return ok;
    }

    public Group getGroupByID(int groupID) throws SQLException{
        Group group = null;
        String select = "SELECT * FROM group_ WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(select);
        ps.setInt(1, groupID);
        ResultSet res = ps.executeQuery();
        if(res.next()){
            group = new Group();
            group.setId(groupID);
            group.setName(res.getString("group_name"));
            group.setNote(res.getString("note"));
        }
        return group;
    }
    public static void main(String[] args){
        GroupDAO dao = new GroupDAO();
        if(!dao.connect()){
            System.out.println("khong ket noi duoc CSDL");
            return;
        }
        Group g = new Group();
        g.setId(2);
        try {
            System.out.println(dao.get(g));
            System.out.println(g);
            dao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
}
