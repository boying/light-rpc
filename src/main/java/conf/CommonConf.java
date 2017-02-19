package conf;

import lombok.Data;

import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@Data
public class CommonConf {
    private InetSocketAddress registryAddress;
}
