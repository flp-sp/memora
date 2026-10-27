package backend.service;

import java.sql.SQLException;
import java.util.List;
import backend.model.Prisoner;
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
                    String jail,
                    String administrativePending,
                    String attorney){
        try{
            prisonerDao.insert(lawsuitNum, prisonerName, dutyDate, sentenceType, dateLastAnalysis, dateLastService, dateTransferApac, jail, administrativePending, attorney);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
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
                    String administrativePending,
                    String attorney){
        try{
            prisonerDao.update(id, lawsuitNum, prisonerName, dutyDate, sentenceType, dateLastAnalysis, dateLastService, dateTransferApac, jail, administrativePending, attorney);              
        }catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public void delete(long id){
        try{
            prisonerDao.delete(id);
        }catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public List<Prisoner> getList(){
        try{
            return prisonerDao.list();
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }
}
