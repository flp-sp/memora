package backend.service;

import java.sql.SQLException;

import backend.exception.ServiceException;
import backend.repositories.LogsDao;

public class LogsService {
    LogsDao logsDao = new LogsDao();

    public void create(Long idUser, Long idPrisoner, String whitelistedEmail, String description, Long idRegister){
        try{
            logsDao.insert(idUser, idPrisoner, whitelistedEmail, description, idRegister);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }
}
