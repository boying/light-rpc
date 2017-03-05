package light.rpc.server_provider;

import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
public interface IServerProvider {
    InetSocketAddress get();
}
