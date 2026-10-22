package backend.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import backend.model.Credential;
import backend.model.User;

public class UserDao {
    public Optional<Credential> searchCreds(String email) throws SQLException{
        String sql = "SELECT id, user_name, users.email, pass_hash, sys_role FROM users"
                    +" LEFT JOIN whitelisted ON users.email = whitelisted.email"            
                    +" WHERE users.email = ?";

        try (Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, email);
                try (ResultSet rSet = ps.executeQuery()){
                    if(!rSet.next()){
                        return Optional.empty();
                    }
                    return Optional.of(new Credential(dataToObject(rSet), rSet.getString("pass_hash")));
                }
            }
    }


    public void insert(String userName, String email, String passHash) throws SQLException{
        String sql = "INSERT INTO users(user_name, email, pass_hash) VALUES(?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, userName);
                ps.setString(2, email);
                ps.setString(3, passHash);
                ps.executeUpdate();
            }
    }


    public long count() throws SQLException{
        long totalUsers;
        String sql = "SELECT COUNT(*) FROM users";
        try(Connection connection = Database.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)){
            ResultSet rSet = ps.executeQuery();
            rSet.next();
            totalUsers = rSet.getLong(1);
            return totalUsers;
        }
    }

    public List<User> list() throws SQLException{
        String sql = "SELECT id, user_name, users.email, pass_hash, sys_role FROM users"
                        +" LEFT JOIN whitelisted ON users.email = whitelisted.email";
        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rSet = ps.executeQuery();
                List<User> list = new ArrayList<>();
                while (rSet.next()){
                    list.add(dataToObject(rSet));
                }
                return list;
            }
    }    

    public User dataToObject(ResultSet rSet) throws SQLException{
        return new User(
            rSet.getLong("id"),
            rSet.getString("user_name"),
            rSet.getString("email"),
            rSet.getString("pass_hash"),
            rSet.getString("sys_role").charAt(0)
        );
    }
}
