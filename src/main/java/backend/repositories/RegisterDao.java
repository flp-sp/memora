package backend.repositories;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import backend.model.Prisoner;
import backend.model.Register;

public class RegisterDao {
    public void insert(long idUser, long idPrisoner, String description, String observations) throws SQLException{
        String sql = "INSERT INTO register(id_user, id_prisoner, date_register, description, observations) VALUES(?, ?, ?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setLong(1, idUser);
                ps.setLong(2, idPrisoner);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setString(4, description);
                ps.setString(5, observations);
                ps.executeUpdate();
            }
    }

    public List<Register> list(Long id) throws SQLException{
        String sql = "SELECT * FROM register WHERE id_prisoner = "+ id +" ORDER BY date_register";
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
            rSet.getDate("date_register").toLocalDate(),
            rSet.getString("description"),
            rSet.getString("observations")
        );
    }
}
