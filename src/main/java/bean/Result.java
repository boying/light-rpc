package bean;

import lombok.Data;
import lombok.ToString;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
@Data
@ToString
public class Result {
    private boolean invokedSuccess;
    private Object result;
    private Throwable throwable;
    private String errorMsg;

    public static Result invokedFailed(String msg) {
        Result result = new Result();
        result.invokedSuccess = false;
        result.errorMsg = msg;
        return result;
    }

    public static Result invokedSuccess(Object result, Throwable throwable) {
        Result ret = new Result();
        ret.invokedSuccess = true;
        ret.result = result;
        ret.throwable = throwable;
        return ret;
    }

}
