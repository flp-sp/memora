package backend.service;

import java.sql.SQLException;
import java.util.List;

import backend.repositories.WhitelistedUserDao;
import backend.exception.ServiceException;
import backend.model.WhiteListed;

public class WhitelistedUserService{
    WhitelistedUserDao whitelistedUserDao = new WhitelistedUserDao();

    public void create(String email, char currentStatus, char sysRole){
        try{
            whitelistedUserDao.insert(email.trim(), currentStatus, sysRole);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public void update(String email, char status, char sysRole){
        try{
            whitelistedUserDao.update(email, status, sysRole);    
        }catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public boolean checkEmailStatus(String email) {
        try {
            WhiteListed wl = whitelistedUserDao.searchEmail(email);

            if (wl == null) {
                return false;
            }

            return wl.currentStatus() == 'O';
        }
        catch (SQLException e) {
            throw ServiceException.exceptionHanddler(e);
        }
    }

    public List<WhiteListed> getList(){
        try{
            return whitelistedUserDao.list();
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }
}