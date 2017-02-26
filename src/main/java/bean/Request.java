package bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@Data
public class Request {
    private boolean async;
    private long asyncReqId;
    private int asyncPort;

    private String iface;
    private String method;
    private List<TypeValue> args;
}
