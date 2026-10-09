package backend.service;

import java.sql.SQLException;

import backend.repositories.WhitelistedUserDao;
import backend.exception.ServiceException;

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
}