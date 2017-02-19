package server_provider;

import conf.ServerProviderConf;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
public class ListedServerProvider implements IServerProvider {
    private AtomicInteger idx = new AtomicInteger();
    private List<InetSocketAddress> list;

    public ListedServerProvider(List<InetSocketAddress> list) {
        this.list = list;
    }

    @Override
    public InetSocketAddress get() {
        if(idx.get() == Integer.MAX_VALUE){
            idx.set(0);
        }
        int i = idx.getAndIncrement() % list.size();
        return list.get(i);
    }
}
