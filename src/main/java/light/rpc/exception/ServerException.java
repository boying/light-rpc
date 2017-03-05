package light.rpc.exception;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class ServerException extends RuntimeException{
    public ServerException() {
        super();
    }

    public ServerException(String message) {
        super(message);
    }
}
