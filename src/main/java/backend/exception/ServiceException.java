package backend.exception;

public class ServiceException extends RuntimeException {
    public ServiceException(String mensagem) {
        super(mensagem);
    }
}