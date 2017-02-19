package register;

import conf.CommonConf;
import conf.ServerConf;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import util.ZooKeeperFactory;

import java.net.Inet4Address;

/**
 * Created by jiangzhiwen on 17/2/19.
 */
public class ZooKeeperRegister implements Register{
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 5000;
    private static final int SESSION_TIMEOUT_MILLISECONDS = 5000;
    private static final String APP_ROOT_PATH = "/app_root/";

    @Override
    public void register(CommonConf commonConf, ServerConf serverConf) throws Exception{
        ZooKeeper zooKeeper = ZooKeeperFactory.getZooKeeper(commonConf.getRegistryAddress(), CONNECTION_TIMEOUT_MILLISECONDS, SESSION_TIMEOUT_MILLISECONDS);
        String appPath = APP_ROOT_PATH + serverConf.getAppId();
        Stat exists = zooKeeper.exists(appPath, false);
        if(exists == null){
            try {
                zooKeeper.create(appPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }catch (Exception e){
                if(zooKeeper.exists(appPath, false) == null){
                    throw e;
                }
            }
        }

        String providerAddress = APP_ROOT_PATH + serverConf.getAppId() + "/" + Inet4Address.getLocalHost().getHostAddress() + ":" + serverConf.getPort();
        zooKeeper.create(providerAddress, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

}
