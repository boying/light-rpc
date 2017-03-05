package light.rpc.server_address_provider;

import java.net.InetSocketAddress;

/**
 * Rpc服务方地址提供方接口
 */
public interface IServerAddressProvider {
    /**
     * 获取Rpc服务地址
     *
     * @return 服务地址
     */
    InetSocketAddress get();
}
