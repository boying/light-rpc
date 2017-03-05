package light.rpc.exception;

/**
 * 客户端任务超时异常
 */
public class ClientTimeoutException extends ClientException{
    public ClientTimeoutException() {
        super();
    }

    public ClientTimeoutException(Exception e) {
        super(e);
    }
}
