package backend.repositories;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import backend.model.Prisoner;

public class PrisonerDao {
    public void insert(String lawsuitNum,
                    String prisonerName,
                    String dutyDate,
                    String sentenceType,
                    String dateLastAnalysis,
                    String dateLastService,
                    String dateTransferApac,
                    String jail,
                    boolean adiministrativePending) throws SQLException{
        String sql = "INSERT INTO prisoner(lawsuit_number, prisoner_name, duty_date, sentence_type, date_last_analysis, date_last_service, date_transfer_apac, jail, administrative_pending) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, lawsuitNum);
                ps.setString(2, prisonerName);
                ps.setDate(3, Date.valueOf(convertToDate(dutyDate)));
                ps.setString(4, convertToChar(sentenceType));
                ps.setDate(5, Date.valueOf(convertToDate(dateLastAnalysis)));
                ps.setDate(6, Date.valueOf(convertToDate(dateLastService)));
                ps.setObject(7, convertToDate(dateTransferApac));
                ps.setString(8, convertToChar(jail));
                ps.setBoolean(9, adiministrativePending);
                ps.executeUpdate();
            }
    }

    public void update(long id, 
                    String lawsuitNum,
                    String prisonerName,
                    String dutyDate,
                    String sentenceType,
                    String dateLastAnalysis,
                    String dateLastService,
                    String dateTransferApac,
                    String jail,
                    boolean adiministrativePending) throws SQLException{

        String sql = "UPDATE prisoner SET lawsuit_number = ?, prisoner_name = ?,"
                    +"duty_date = ?, sentence_type = ?, date_last_analysis = ?, "
                    +"date_last_service = ?, date_transfer_apac = ?, jail = ?, administrative_pending = ? WHERE id = ?";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, lawsuitNum);
                ps.setString(2, prisonerName);
                ps.setDate(3, Date.valueOf(convertToDate(dutyDate)));
                ps.setString(4, convertToChar(sentenceType));
                ps.setDate(5, Date.valueOf(convertToDate(dateLastAnalysis)));
                ps.setDate(6, Date.valueOf(convertToDate(dateLastService)));
                ps.setObject(7, convertToDate(dateTransferApac));
                ps.setString(8, convertToChar(jail));
                ps.setBoolean(9, adiministrativePending);
                ps.setLong(10, id);
                ps.executeUpdate();
            }
    }

    public void delete(long id) throws SQLException{
        String sql = "DELETE FROM prisoner WHERE id = ?";
        
        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setLong(1, id);
                ps.executeUpdate();
            }
    }

    public List<Prisoner> list() throws SQLException{
        String sql = "SELECT * FROM prisoner ORDER BY date_last_analysis";
        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ResultSet rSet = ps.executeQuery();
                List<Prisoner> list = new ArrayList<>();
                while (rSet.next()){
                    list.add(dataToObject(rSet));
                }
                return list;
            }
    }

    public Prisoner dataToObject(ResultSet rSet) throws SQLException{
        return new Prisoner(
            rSet.getLong("id"),
            rSet.getString("lawsuit_number"),
            rSet.getString("prisoner_name"),
            rSet.getDate("duty_date").toLocalDate(),
            rSet.getString("sentence_type").charAt(0),
            rSet.getDate("date_last_analysis").toLocalDate(),
            rSet.getDate("date_last_service").toLocalDate(),
            rSet.getDate("date_transfer_apac") != null ? rSet.getDate("date_transfer_apac").toLocalDate() : null,
            rSet.getString("jail").charAt(0),
            rSet.getBoolean("administrative_pending"));
    }

    private String convertToChar(String var){
        switch (var) {
            case "Progressão semiaberto":
                return "S";
            case "Progressão aberto":
                return "A";
            case "Livramento condicional":
                return "L";
            case "APAC":
                return "A";
            case "UPTIM":
                return "U";             
            default:
                return null;
        }
    }

    private LocalDate convertToDate(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
        if(dateString.isBlank() || dateString == ""){
            return null;
        }
        return LocalDate.parse(dateString, formatter);
    }
}
