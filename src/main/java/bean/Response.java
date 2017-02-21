package bean;

import lombok.Data;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@Data
public class Response {
    private boolean invokedSuccess;
    private String result;
    private String throwable;
    private String errorMsg;
}
