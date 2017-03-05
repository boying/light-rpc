package light.rpc.exception;

/**
 * 服务端异常
 */
public class ServerException extends RuntimeException{
    public ServerException() {
        super();
    }

    public ServerException(String message) {
        super(message);
    }
}
