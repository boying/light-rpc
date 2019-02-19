package test;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jiangzhiwen on 17/2/15.
 */
public class ZookeeperTest {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        String path = "/path1";
        final CountDownLatch latch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 5000, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                System.out.println("watcher triggered: " + watchedEvent);
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    if (watchedEvent.getType() == Event.EventType.None && watchedEvent.getPath() == null) {
                        latch.countDown();
                    }
                }
            }
        });
        latch.await();
        System.out.println("ok");

        Stat exists = zooKeeper.exists(path, new MyWatcher(zooKeeper));
        System.out.println(exists);

        String path2 = "/txt";
        exists = zooKeeper.exists(path2, new MyWatcher(zooKeeper));
        System.out.println(exists);

        List<String> children = zooKeeper.getChildren(path2, new MyWatcher(zooKeeper));
        System.out.println(children);


        Thread.sleep(Integer.MAX_VALUE);
    }

    public static class MyWatcher implements Watcher{
        private final ZooKeeper zooKeeper;

        public MyWatcher(ZooKeeper zooKeeper) {
            this.zooKeeper = zooKeeper;
        }

        @Override
        public void process(WatchedEvent event) {
            System.out.println(event);
            try {
                //zooKeeper.exists(event.getPath(), this);
                List<String> children = zooKeeper.getChildren(event.getPath(), this);
                System.out.println(children);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
