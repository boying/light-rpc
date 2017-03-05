package light.rpc.conf.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class Client {
    private String appId;
    private String protocol;
    private int threadPoolSize;
    private int methodDefaultTimeoutMillisecond;
    private List<IpPort> serverProviders;
    private List<Interface> interfaces;
}
