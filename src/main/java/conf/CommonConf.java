package conf;

import client.async.AsyncCallFutureContainer;
import lombok.Data;

import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@Data
public class CommonConf {
    private InetSocketAddress registryAddress;
    private int asyncClientPort;
    private AsyncCallFutureContainer asyncCallFutureContainer;
}
