package backend.repositories;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PrisonerDao {
    public void insert(String lawsuitNum,
    String prisonerName,
    String dutyDate,
    String sentenceType,
    String dateLastAnalysis,
    String dateLastService,
    String dateTransferApac,
    String jail) throws SQLException{
        String sql = "INSERT INTO prisoner(lawsuit_number, prisoner_name, duty_date, sentence_type, date_last_analysis, date_last_service, date_transfer_apac, jail) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

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
                ps.executeQuery();
            }
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
