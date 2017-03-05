package light.rpc.server_address_provider;

import light.rpc.util.InetSocketAddressFactory;
import light.rpc.util.ZooKeeperFactory;
import lombok.RequiredArgsConstructor;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于ZooKeeper自动发现服务的Rpc服务地址提供者
 */
@RequiredArgsConstructor
public class ZooKeeperServerAddressProvider implements IServerAddressProvider {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperServerAddressProvider.class);

    /**
     * ZooKeeper服务器地址
     */
    private final InetSocketAddress zooKeeperAddress;

    /**
     * 关注的服务id
     */
    private final String appId;

    private ZooKeeper zooKeeper;
    private volatile boolean initialized = false;
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 5000;
    private static final int SESSION_TIMEOUT_MILLISECONDS = 50000;
    private static final String APP_ROOT_PATH = "/app_root/";
    private String appPath;
    private List<InetSocketAddress> serverProviderAddresses = new ArrayList<>();
    private AtomicInteger confIndex = new AtomicInteger(0);
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * 初始化
     */
    public void init() {
        try {
            innerInit();
            logger.debug("start successfully");
        } catch (Exception e) {
            logger.debug("start failed");
            throw new RuntimeException("start failed", e);
        }
    }

    private void innerInit() throws IOException, InterruptedException, KeeperException {
        this.appPath = APP_ROOT_PATH + appId;
        zooKeeper = ZooKeeperFactory.getZooKeeper(zooKeeperAddress, CONNECTION_TIMEOUT_MILLISECONDS, SESSION_TIMEOUT_MILLISECONDS);

        List<String> children1 = zooKeeper.getChildren("/", false);
        logger.debug("{}", children1);

        Stat exists = zooKeeper.exists(appPath, false);
        if (exists == null) {
            throw new IllegalStateException("there's not app path in zookeeper");
        }

        List<String> children = zooKeeper.getChildren(appPath, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    List<String> nodeNames;
                    try {
                        nodeNames = zooKeeper.getChildren(event.getPath(), this);
                        initProviders(nodeNames);
                    } catch (Exception e) {
                        logger.warn("ZooKeeper error happened, ", e);
                    }
                }
            }
        });

        initProviders(children);

        initialized = true;
    }

    private void initProviders(List<String> nodeNames) {
        readWriteLock.writeLock().lock();
        try {
            serverProviderAddresses.clear();
            for (String nodeName : nodeNames) {
                InetSocketAddress serverProviderConf = InetSocketAddressFactory.get(nodeName);
                serverProviderAddresses.add(serverProviderConf);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public InetSocketAddress get() {
        if (!initialized) {
            throw new IllegalStateException("ZooKeeperServerAddressProvider not initialized;");
        }

        if (serverProviderAddresses.size() == 0) {
            return null;
        }

        try {
            readWriteLock.readLock().lock();
            return serverProviderAddresses.get(Math.abs(confIndex.incrementAndGet()) % serverProviderAddresses.size());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
