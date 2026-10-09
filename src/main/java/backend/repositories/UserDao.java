package backend.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {
    public void insert(String user_name, String email, String pass_hash) throws SQLException{
        String sql = "INSERT INTO users(user_name, email, pass_hash) VALUES(?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, user_name);
                ps.setString(2, email);
                ps.setString(3, pass_hash);
                ps.executeQuery();
            }
    }
}
