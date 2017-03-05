package light.rpc.result;

import lombok.Data;
import lombok.ToString;

/**
 * rpc调用结果类
 */
@Data
@ToString
public class Result {
    /**
     * 是否是异步调用
     */
    private boolean async;

    /**
     * 异步调用的请求id
     */
    private long asyncReqId;

    /**
     * 是否调用成功
     */
    private boolean invokedSuccess;

    /**
     * 成功调用的返回值
     */
    private Object result;

    /**
     * 成功调用抛出的异常
     */
    private Throwable throwable;

    /**
     * 异常类型
     */
    private Class<? extends Throwable> throwableType;

    /**
     * 没有成功调用,返回的错误信息,此错误是框架抛出的错误,而不是实际调用函数中出现的错误
     */
    private String errorMsg;

    /**
     * 创建未能成功调用的返回结果对象
     *
     * @param msg 错误信息
     * @return
     */
    public static Result invokedFailed(String msg) {
        Result result = new Result();
        result.invokedSuccess = false;
        result.errorMsg = msg;
        return result;
    }

    /**
     * 创建成功调用的返回结果对象
     *
     * @param result    调用结果
     * @param throwable 调用抛出的异常
     * @return
     */
    public static Result invokedSuccess(Object result, Throwable throwable, Class<? extends Throwable> throwableType) {
        Result ret = new Result();
        ret.invokedSuccess = true;
        ret.result = result;
        ret.throwable = throwable;
        ret.throwableType = throwableType;
        return ret;
    }

}
