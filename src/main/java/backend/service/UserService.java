package backend.service;

import java.sql.SQLException;
import java.util.Optional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import backend.repositories.Database;
import at.favre.lib.crypto.bcrypt.BCrypt;
import backend.repositories.UserDao;

import backend.exception.ServiceException;
import backend.model.Credential;
import backend.model.User;

public class UserService {
    private static final int CUSTO_BCRYPT = 12;
    private final UserDao userDao = new UserDao();


    public void ensureFirstUser(){
        try{
            if(userDao.count() == 0){
                String sql = "INSERT INTO whitelisted(email, current_status, sys_role) VALUES('null@g.c', 'A', 'A');INSERT INTO users(user_name, email, pass_hash) VALUES('Admin', 'null@g.c', '$2a$12$UGmrRsw51wre8p7URaNJl./buCT/W0bMRiMP/q.5hFE7fbuC51Jxe')";

                try(Connection connection = Database.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)){
                        ps.executeQuery();
                    }
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

