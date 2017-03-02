package server_provider;

import lombok.RequiredArgsConstructor;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import util.InetSocketAddressFactory;
import util.ZooKeeperFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@RequiredArgsConstructor
public class ZooKeeperServerProvider implements IServerProvider {
    private final InetSocketAddress zookeeperAddress;
    private final String appId;

    private ZooKeeper zooKeeper;
    private volatile boolean initialized = false;
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 5000;
    private static final int SESSION_TIMEOUT_MILLISECONDS = 50000;
    private static final String APP_ROOT_PATH = "/app_root/";
    private String appPath;
    private List<InetSocketAddress> serverProviderAddresses = new ArrayList<>();
    private int confIndex;

    public void init() {
        try {
            innerInit();
        } catch (Exception e) {
            throw new RuntimeException("init failed", e);
        }
    }

    private void innerInit() throws IOException, InterruptedException, KeeperException {
        this.appPath = APP_ROOT_PATH + appId;
        zooKeeper = ZooKeeperFactory.getZooKeeper(zookeeperAddress, CONNECTION_TIMEOUT_MILLISECONDS, SESSION_TIMEOUT_MILLISECONDS);

        List<String> children1 = zooKeeper.getChildren("/", false);
        System.out.println(children1);

        Stat exists = zooKeeper.exists(appPath, false);
        if (exists == null) {
            throw new RuntimeException("there's not app path in zookeeper");
        }

        List<String> children = zooKeeper.getChildren(appPath, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                }
                List<String> nodeNames = null;
                try {
                    nodeNames = zooKeeper.getChildren(event.getPath(), this);
                    initProviders(nodeNames);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        initProviders(children);

        initialized = true;
    }


    private synchronized void initProviders(List<String> nodeNames) {
        confIndex = 0;
        serverProviderAddresses.clear();
        for (String nodeName : nodeNames) {
            InetSocketAddress serverProviderConf = InetSocketAddressFactory.get(nodeName);
            if (serverProviderConf == null) {
                // TODO log warning
            } else {
                serverProviderAddresses.add(serverProviderConf);
            }
        }
    }

    @Override
    public synchronized InetSocketAddress get() {
        if (!initialized) {
            throw new RuntimeException("ZooKeeperServerProvider not initialized;");
        }

        if (serverProviderAddresses.size() == 0) {
            return null;
        }

        if (confIndex == Integer.MAX_VALUE) {
            confIndex = 0;
        }

        return serverProviderAddresses.get(confIndex++ % serverProviderAddresses.size());
    }
}
