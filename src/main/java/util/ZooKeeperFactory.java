package util;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangzhiwen on 17/2/19.
 */
public class ZooKeeperFactory {
    public static ZooKeeper getZooKeeper(InetSocketAddress zookeeperAddress, int connectionTimeoutMilliseconds, int sessionTimeoutMilliseconds) throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(zookeeperAddress.getAddress().getHostAddress() + ":" + zookeeperAddress.getPort(), sessionTimeoutMilliseconds, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    if (watchedEvent.getType() == Event.EventType.None && watchedEvent.getPath() == null) {
                        latch.countDown();
                    }
                }
            }
        });
        latch.await(connectionTimeoutMilliseconds, TimeUnit.MILLISECONDS);

        return zooKeeper;
    }
}
