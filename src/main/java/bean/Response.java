package bean;

import lombok.Data;
import lombok.ToString;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@Data
@ToString
public class Response {
    private boolean async;
    private long asyncReqId;

    private boolean invokedSuccess;
    private String result;
    private String throwable;
    private String errorMsg;
}
