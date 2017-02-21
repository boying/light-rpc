package exception;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class ClientException extends RuntimeException{
    public ClientException() {
        super();
    }

    public ClientException(Exception e) {
        super(e);
    }
}
