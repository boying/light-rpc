package light.rpc.service_register;

import light.rpc.conf.Config;

/**
 * 服务注册接口,用于将Rpc服务注册到注册中心
 */
public interface Register {
    /**
     * 注册服务
     *
     * @param registry 注册中心的conf
     * @param server 服务的conf
     * @throws Exception
     */
    void register(Config.Registry registry, Config.Server server) throws Exception;

    /**
     * 取消服务
     *
     * @param registry 注册中心的conf
     * @param server 服务的conf
     * @throws Exception
     */
    void unRegister(Config.Registry registry, Config.Server server) throws Exception;
}
