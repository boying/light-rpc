package bean;

import lombok.Data;

/**
 * Created by jiangzhiwen on 17/2/27.
 */
@Data
public class AsyncResponse {
    private long asyncReqId;
    private boolean acceptedSuccess;
    private String errorMsg;

}
