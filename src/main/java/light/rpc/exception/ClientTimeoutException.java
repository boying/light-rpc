package light.rpc.exception;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class ClientTimeoutException extends ClientException{
    public ClientTimeoutException() {
        super();
    }

    public ClientTimeoutException(Exception e) {
        super(e);
    }
}
