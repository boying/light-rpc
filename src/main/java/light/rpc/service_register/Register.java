package light.rpc.service_register;

import light.rpc.conf.CommonConf;
import light.rpc.conf.ServerConf;

/**
 * 服务注册接口,用于将Rpc服务注册到注册中心
 */
public interface Register {
    /**
     * 注册服务
     *
     * @param commonConf 注册中心的conf
     * @param serverConf 服务的conf
     * @throws Exception
     */
    void register(CommonConf commonConf, ServerConf serverConf) throws Exception;

    /**
     * 取消服务
     *
     * @param commonConf 注册中心的conf
     * @param serverConf 服务的conf
     * @throws Exception
     */
    void unRegister(CommonConf commonConf, ServerConf serverConf) throws Exception;
}
