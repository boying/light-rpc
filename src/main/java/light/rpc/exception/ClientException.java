package light.rpc.exception;

/**
 * 客户端发生的异常
 */
public class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }

    public ClientException() {
        super();
    }

    public ClientException(Exception e) {
        super(e);
    }
}
