package backend.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import backend.model.Logs;

public class LogsDao {
    public void insert(long idUser, Long idPrisoner, String whitelistedEmail, String description, Long idRegister) throws SQLException{
        String sql = "INSERT INTO logs(id_user, id_prisoner, whitelisted_email, description, date_time, id_register) VALUES(?, ? , ? ,? , ?, ?)";
        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setLong(1, idUser);
                ps.setObject(2, idPrisoner, Types.INTEGER);
                ps.setString(3, whitelistedEmail);
                ps.setString(4, description);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(6, idRegister, Types.INTEGER);
                ps.executeUpdate();
            }
    }

    private Logs dataToObject(ResultSet rSet) throws SQLException{
        return new Logs(rSet.getLong("id"),
                        rSet.getLong("id_user"),
                        rSet.getLong("id_prisoner"),
                        rSet.getString("whitelisted_email"),
                        rSet.getString("description"),
                        rSet.getTimestamp("date_time").toLocalDateTime(),
                        rSet.getLong("id_register")
            
        );
    }
}