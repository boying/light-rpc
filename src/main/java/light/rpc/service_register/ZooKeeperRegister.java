package light.rpc.service_register;

import light.rpc.conf.Config;
import light.rpc.util.ZooKeeperFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.net.Inet4Address;

/**
 * ZooKeeper服务注册对象,将服务注册到ZooKeeper
 */
public class ZooKeeperRegister implements Register {
    /**
     * 链接超时毫秒
     */
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 5000;

    /**
     * 连接保持超时毫秒
     */
    private static final int SESSION_TIMEOUT_MILLISECONDS = 5000;

    /**
     * 服务注册路径
     */
    private static final String APP_ROOT_PATH = "/app_root/";

    @Override
    public void register(Config.Registry commonConf, Config.Server serverConf) throws Exception {
        ZooKeeper zooKeeper = ZooKeeperFactory.getZooKeeper(commonConf.getAddress(), CONNECTION_TIMEOUT_MILLISECONDS, SESSION_TIMEOUT_MILLISECONDS);
        String appPath = APP_ROOT_PATH + serverConf.getAppId();
        Stat exists = zooKeeper.exists(appPath, false);
        if (exists == null) {
            try {
                zooKeeper.create(appPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (Exception e) {
                if (zooKeeper.exists(appPath, false) == null) {
                    throw e;
                }
            }
        }

        String providerAddress = APP_ROOT_PATH + serverConf.getAppId() + "/" + Inet4Address.getLocalHost().getHostAddress() + ":" + serverConf.getPort();
        if (zooKeeper.exists(providerAddress, false) == null) {
            zooKeeper.create(providerAddress, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }
    }

    @Override
    public void unRegister(Config.Registry commonConf,  Config.Server  serverConf) throws Exception {
        ZooKeeper zooKeeper = ZooKeeperFactory.getZooKeeper(commonConf.getAddress(), CONNECTION_TIMEOUT_MILLISECONDS, SESSION_TIMEOUT_MILLISECONDS);
        String appPath = APP_ROOT_PATH + serverConf.getAppId();
        Stat exists = zooKeeper.exists(appPath, false);
        if (exists == null) {
            return;
        }

        String providerAddress = APP_ROOT_PATH + serverConf.getAppId() + "/" + Inet4Address.getLocalHost().getHostAddress() + ":" + serverConf.getPort();
        if (zooKeeper.exists(providerAddress, false) == null) {
            return;
        }

        zooKeeper.delete(providerAddress, -1);
    }
}
