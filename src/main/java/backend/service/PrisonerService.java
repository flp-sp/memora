package backend.service;

import java.sql.SQLException;

import backend.exception.ServiceException;
import backend.repositories.PrisonerDao;

public class PrisonerService {
    PrisonerDao prisonerDao = new PrisonerDao();

    public void create(String lawsuitNum,
    String prisonerName,
    String dutyDate,
    String sentenceType,
    String dateLastAnalysis,
    String dateLastService,
    String dateTransferApac,
    String jail){
        try{
            prisonerDao.insert(lawsuitNum, prisonerName, dutyDate, sentenceType, dateLastAnalysis, dateLastService, dateTransferApac, jail);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }
}
