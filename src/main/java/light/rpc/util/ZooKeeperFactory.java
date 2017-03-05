package light.rpc.util;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 创建ZooKeeper对象工厂
 */
public class ZooKeeperFactory {
    /**
     * 创建连接好的ZooKeeper对象
     *
     * @param zooKeeperAddress              ZooKeeper 服务器地址
     * @param connectionTimeoutMilliseconds 连接超时毫秒
     * @param sessionTimeoutMilliseconds    会话超时毫秒
     * @return ZooKeeper对象
     * @throws IOException
     * @throws InterruptedException
     */
    public static ZooKeeper getZooKeeper(InetSocketAddress zooKeeperAddress, int connectionTimeoutMilliseconds, int sessionTimeoutMilliseconds) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(zooKeeperAddress.getAddress().getHostAddress() + ":" + zooKeeperAddress.getPort(), sessionTimeoutMilliseconds, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    if (watchedEvent.getType() == Event.EventType.None && watchedEvent.getPath() == null) {
                        latch.countDown();
                    }
                }
            }
        });

        if (!latch.await(connectionTimeoutMilliseconds, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("ZooKeeper conn timeout");
        }

        return zooKeeper;
    }
}
