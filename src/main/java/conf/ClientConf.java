package conf;

import lombok.Data;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class ClientConf {
    private String appId;
    private Protocol protocol;
    private int threadPoolSize;
    private Integer methodDefaultTimeoutMillisecond;
    private List<InetSocketAddress> serverProviders;
    private List<InterfaceConf> interfaces;
}
