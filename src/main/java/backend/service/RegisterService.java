package backend.service;

import java.sql.SQLException;
import java.util.List;

import backend.exception.ServiceException;
import backend.model.Prisoner;
import backend.model.Register;
import backend.repositories.RegisterDao;

public class RegisterService {
    RegisterDao registerDao = new RegisterDao();

    public void create(long idUser, long idPrisoner, String description, String observations){
        try{
            registerDao.insert(idUser, idPrisoner, description, observations);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public List<Register> getList(Long id){
        try{
            return registerDao.list(id);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }
}
