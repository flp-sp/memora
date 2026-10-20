package backend.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import backend.model.WhiteListed;

public class WhitelistedUserDao {
    public void insert(String email, char currentStatus, char sysRole) throws SQLException{
        String sql = "INSERT INTO whitelisted(email, current_status, sys_role) VALUES(?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, email);
                ps.setString(2, String.valueOf(currentStatus));
                ps.setString(3, String.valueOf(sysRole));
                ps.executeUpdate();
            }
    }


    public WhiteListed searchEmail(String email) throws SQLException{
        String sql = "SELECT * FROM whitelisted WHERE email = ?";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, email); 

                try (ResultSet rSet = ps.executeQuery()) {
                    if (rSet.next()) {
                        return dataToObject(rSet);
                    }
                    return null;
                }
        }
    }


    private WhiteListed dataToObject(ResultSet rSet) throws SQLException{
        return new WhiteListed(
            rSet.getString("email"),
            rSet.getString("current_status").charAt(0),
            rSet.getString("sys_role").charAt(0)
        );
    }
}
