package light.rpc.server_address_provider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 根据Rpc服务方地址列表,提供地址
 */
@RequiredArgsConstructor
public class ListedServerAddressProvider implements IServerAddressProvider {
    /**
     * 服务方地址列表
     */
    @NonNull
    private final List<InetSocketAddress> list;

    private AtomicInteger idx = new AtomicInteger();

    @Override
    public InetSocketAddress get() {
        int i = Math.abs(idx.getAndIncrement() % list.size());
        return list.get(i);
    }
}
