package light.rpc.exception;

/**
 * 客户端任务被拒绝接受异常
 */
public class ClientTaskRejectedException extends ClientException{
    public ClientTaskRejectedException(String msg) {
        super(msg);
    }
}
