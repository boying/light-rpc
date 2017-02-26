package client.async;

import bean.Result;
import lombok.Data;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@Data
public class AsyncResult {
    private long reqId;
    private Result result;
}
