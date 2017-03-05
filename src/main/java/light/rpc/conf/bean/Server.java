package light.rpc.conf.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
@Data
public class Server {
    private String appId;
    private String protocol;
    private int port;
    private List<String> interfaces;
    private int threadPoolSize;
}
