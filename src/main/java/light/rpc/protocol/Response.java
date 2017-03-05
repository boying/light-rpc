package light.rpc.protocol;

import lombok.Data;
import lombok.ToString;

/**
 * Rpc调用回复类
 */
@Data
@ToString
public class Response {
    /**
     * 是否是异步调用的回复
     */
    private boolean async;

    /**
     * 异步调用请求id
     */
    private long asyncReqId;

    /**
     * 是否调用成功
     */
    private boolean invokedSuccess;

    /**
     * 调用结果的json串
     */
    private String result;

    /**
     * 调用抛出异常的json串
     */
    private String throwable;

    /**
     * 异常类型
     */
    private String throwableType;

    /**
     * invokedSuccess为false的情况下,报错信息
     */
    private String errorMsg;
}
