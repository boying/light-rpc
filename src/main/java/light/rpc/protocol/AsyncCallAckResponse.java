package light.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 在接收到异步调用请求后,返回的确认Response
 */
@Data
@AllArgsConstructor
public class AsyncCallAckResponse {
    /**
     * 异步调用请求的id
     */
    private long asyncReqId;

    /**
     * 是否成功接收异步调用请求
     */
    private boolean acceptedSuccess;

    /**
     * 未能成功接收请求时,错误内容
     */
    private String errorMsg;
}
