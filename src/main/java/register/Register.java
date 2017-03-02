package register;

import conf.CommonConf;
import conf.ServerConf;

/**
 * Created by jiangzhiwen on 17/2/19.
 */
public interface Register {
    void register(CommonConf commonConf, ServerConf serverConf) throws Exception;
}
