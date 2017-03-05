package light.rpc.conf.bean;


import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class Interface {
    private String name;
    private List<Method> methods;
    private int timeoutMillisecond;

}
