package backend.service;

import java.sql.SQLException;
import java.util.Optional;

import at.favre.lib.crypto.bcrypt.BCrypt;
import backend.repositories.UserDao;

import backend.exception.ServiceException;
import backend.model.Credential;
import backend.model.User;

public class UserService {
    private static final int CUSTO_BCRYPT = 12;
    private final UserDao userDao = new UserDao();
    private final WhitelistedUserService whitelistedUserService = new WhitelistedUserService();


    public void ensureFirstUser(){
        try{
            if(userDao.count() == 0){
                whitelistedUserService.create("null@g.c", 'A', 'A');
                create("Admin", "null@g.c", "admin123");
            }
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }


    public void create(String userName, String email, String passString){
        try{
            userDao.insert(userName.trim(), email, gerarHash(passString));
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }


    public Optional<User> auth(String email, String passString){
        if(email == null || email.isBlank() || passString == null || passString.isBlank()){
            return Optional.empty();
        }
        try{
            return userDao.searchCreds(email.trim()).filter(creds -> BCrypt.verifyer()
            .verify(passString.toCharArray(), creds.senhaHash()).verified).map(Credential::user);
        }
        catch(SQLException e){
            throw ServiceException.exceptionHanddler(e);
        }
    }

    // helpers
    private String gerarHash(String senha) {
        return BCrypt.withDefaults().hashToString(CUSTO_BCRYPT, senha.toCharArray());
    }
}

