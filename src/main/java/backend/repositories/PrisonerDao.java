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
                    String adiministrativePending,
                    String attorney) throws SQLException{
        String sql = "INSERT INTO prisoner(lawsuit_number, prisoner_name, duty_date, sentence_type, date_last_analysis, date_last_service, date_transfer_apac, jail, administrative_pending, attorney) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, emptyToNull(lawsuitNum));
                ps.setString(2, prisonerName == null ? "" : prisonerName);
                LocalDate duty = convertToDate(dutyDate);
                if (duty == null) {
                    ps.setNull(3, java.sql.Types.DATE);
                } else {
                    ps.setDate(3, Date.valueOf(duty));
                }
                ps.setString(4, convertToChar(sentenceType));
                LocalDate lastAnalysis = convertToDate(dateLastAnalysis);
                if (lastAnalysis == null) {
                    ps.setNull(5, java.sql.Types.DATE);
                } else {
                    ps.setDate(5, Date.valueOf(lastAnalysis));
                }
                LocalDate lastService = convertToDate(dateLastService);
                if (lastService == null) {
                    ps.setNull(6, java.sql.Types.DATE);
                } else {
                    ps.setDate(6, Date.valueOf(lastService));
                }
                LocalDate transferApac = convertToDate(dateTransferApac);
                if (transferApac == null) {
                    ps.setNull(7, java.sql.Types.DATE);
                } else {
                    ps.setObject(7, transferApac);
                }
                ps.setString(8, convertToChar(jail));
                ps.setString(9, emptyToNull(adiministrativePending));
                ps.setString(10, attorney == null || attorney.isBlank() ? "DPE" : attorney);
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
                    String adiministrativePending,
                    String attorney) throws SQLException{

        String sql = "UPDATE prisoner SET lawsuit_number = ?, prisoner_name = ?,"
                    +"duty_date = ?, sentence_type = ?, date_last_analysis = ?, "
                    +"date_last_service = ?, date_transfer_apac = ?, jail = ?, administrative_pending = ?, attorney = ? WHERE id = ?";

        try(Connection connection = Database.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setString(1, emptyToNull(lawsuitNum));
                ps.setString(2, prisonerName == null ? "" : prisonerName);
                LocalDate duty = convertToDate(dutyDate);
                if (duty == null) {
                    ps.setNull(3, java.sql.Types.DATE);
                } else {
                    ps.setDate(3, Date.valueOf(duty));
                }
                ps.setString(4, convertToChar(sentenceType));
                LocalDate lastAnalysis = convertToDate(dateLastAnalysis);
                if (lastAnalysis == null) {
                    ps.setNull(5, java.sql.Types.DATE);
                } else {
                    ps.setDate(5, Date.valueOf(lastAnalysis));
                }
                LocalDate lastService = convertToDate(dateLastService);
                if (lastService == null) {
                    ps.setNull(6, java.sql.Types.DATE);
                } else {
                    ps.setDate(6, Date.valueOf(lastService));
                }
                LocalDate transferApac = convertToDate(dateTransferApac);
                if (transferApac == null) {
                    ps.setNull(7, java.sql.Types.DATE);
                } else {
                    ps.setObject(7, transferApac);
                }
                ps.setString(8, convertToChar(jail));
                ps.setString(9, emptyToNull(adiministrativePending));
                ps.setString(10, attorney == null || attorney.isBlank() ? "DPE" : attorney);
                ps.setLong(11, id);
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
        String lawsuitNumber = rSet.getString("lawsuit_number");
        if (lawsuitNumber == null) {
            lawsuitNumber = "";
        }
        String prisonerName = rSet.getString("prisoner_name");
        if (prisonerName == null) {
            prisonerName = "";
        }
        java.sql.Date dutyDateSql = rSet.getDate("duty_date");
        LocalDate dutyDate = dutyDateSql == null ? null : dutyDateSql.toLocalDate();
        String sentenceTypeStr = rSet.getString("sentence_type");
        char sentenceType = sentenceTypeStr == null || sentenceTypeStr.isEmpty() ? 0 : sentenceTypeStr.charAt(0);
        java.sql.Date lastAnalysisSql = rSet.getDate("date_last_analysis");
        LocalDate lastAnalysis = lastAnalysisSql == null ? null : lastAnalysisSql.toLocalDate();
        java.sql.Date lastServiceSql = rSet.getDate("date_last_service");
        LocalDate lastService = lastServiceSql == null ? null : lastServiceSql.toLocalDate();
        java.sql.Date transferApacSql = rSet.getDate("date_transfer_apac");
        LocalDate transferApac = transferApacSql == null ? null : transferApacSql.toLocalDate();
        String jailStr = rSet.getString("jail");
        char jail = jailStr == null || jailStr.isEmpty() ? 0 : jailStr.charAt(0);
        String adminPending = rSet.getString("administrative_pending");
        if (adminPending == null) {
            adminPending = "";
        }
        String attorney = rSet.getString("attorney");
        if (attorney == null || attorney.isBlank()) {
            attorney = "DPE";
        }
        return new Prisoner(
            rSet.getLong("id"),
            lawsuitNumber,
            prisonerName,
            dutyDate,
            sentenceType,
            lastAnalysis,
            lastService,
            transferApac,
            jail,
            adminPending,
            attorney);
    }

    private String convertToChar(String var){
        if (var == null || var.isBlank()) {
            return null;
        }
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
            case "Regime Aberto":
                return "R";
            default:
                return null;
        }
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private LocalDate convertToDate(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(java.time.format.ResolverStyle.STRICT);
        if(dateString == null || dateString.isBlank() || dateString.equals("")){
            return null;
        }
        try {
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
