package register;

import conf.CommonConf;
import conf.ServerConf;

import java.io.IOException;

/**
 * Created by jiangzhiwen on 17/2/19.
 */
public interface Register {
    void register(CommonConf commonConf, ServerConf serverConf) throws Exception;
}
