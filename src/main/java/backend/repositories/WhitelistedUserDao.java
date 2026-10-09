package backend.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WhitelistedUserDao {
    public void insert(String email, char currentStatus, char sysRole) throws SQLException{
        String sql = "INSERT INTO users(email, current_status, sys_role) VALUES(?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, email);
                ps.setString(2, String.valueOf(currentStatus));
                ps.setString(3, String.valueOf(sysRole));
                ps.executeQuery();
            }
    }
}
