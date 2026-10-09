package backend.exception;

import java.sql.SQLException;

public class ServiceException extends RuntimeException {
    public ServiceException(String mensagem) {
        super(mensagem);
    }


    public static ServiceException exceptionHanddler(SQLException e){
        return new ServiceException("Erro no banco de dados: " + e.getMessage());
    }
}