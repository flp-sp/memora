package backend.service;

import java.sql.SQLException;
import java.util.List;

import backend.exception.ServiceException;
import backend.model.Prisoner;
import backend.model.Register;
import backend.model.User;
import backend.repositories.RegisterDao;
import backend.repositories.UserDao;

public class RegisterService {
    RegisterDao registerDao = new RegisterDao();
    UserDao userDao = new UserDao();

    public void create(long idUser, long idPrisoner, String description, String observations){
        try{
            registerDao.insert(idUser, idPrisoner, description, observations);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public void update(long id, long idUser, String description, String observations){
        try{
            registerDao.update(id, idUser, description, observations);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public void delete(long id, long idUserDel){
        try{
            registerDao.delete(id, idUserDel);
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

    public String getUserName(Long id){
        try{
            return userDao.getUserNameById(id);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }
}
