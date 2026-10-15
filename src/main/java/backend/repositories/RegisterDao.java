package backend.repositories;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import backend.model.Register;
import backend.service.LogsService;

public class RegisterDao {
    LogsService logsService = new LogsService();

    public void insert(long idUser, long idPrisoner, String description, String observations) throws SQLException{
        String sql = "INSERT INTO register(id_user, id_prisoner, date_time_register, description, observations) VALUES(?, ?, ?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setLong(1, idUser);
                ps.setLong(2, idPrisoner);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(4, description);
                ps.setString(5, observations);
                ps.executeUpdate();

                logsService.create(idUser, idPrisoner, null, "Insert register", null);
            }
    }

    public void update(long id, long idUserEdit, String description, String observations) throws SQLException{
        String sql = "UPDATE register SET id_user_edit = ?, date_time_register = ?, description = ?,"
                                        +"observations = ? WHERE id = ?";
        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setLong(1, idUserEdit);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, description);
                ps.setString(4, observations);
                ps.setLong(5, id);
                ps.executeUpdate();

                logsService.create(idUserEdit, null, null, "Update register", id);
            }
    }

    public void delete(long id, long idUserDel) throws SQLException{
        String sql = "DELETE FROM register WHERE id = " + id;

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.executeUpdate();
            }
        logsService.create(idUserDel, null, null, "Delete register", null);
    }

    public List<Register> list(Long id) throws SQLException{
        String sql = "SELECT * FROM register WHERE id_prisoner = "+ id +" ORDER BY date_time_register";
        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rSet = ps.executeQuery();
                List<Register> list = new ArrayList<>();
                while (rSet.next()){
                    list.add(dataToObject(rSet));
                }
                return list;
            }
    }

    private Register dataToObject(ResultSet rSet) throws SQLException{
        return new Register(
            rSet.getLong("id"),
            rSet.getLong("id_user"),
            rSet.getLong("id_prisoner"),
            rSet.getTimestamp("date_time_register").toLocalDateTime(),
            rSet.getString("description"),
            rSet.getString("observations"),
            rSet.getLong("id_user_edit")
        );
    }
}
