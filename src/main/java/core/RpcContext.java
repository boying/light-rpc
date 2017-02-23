package core;

import client.Client;
import conf.ClientConf;
import conf.Conf;
import conf.ConfParser;
import conf.Protocol;
import register.Register;
import register.ZooKeeperRegister;
import server.HttpServer;
import server.Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
public class RpcContext {
    private String confPath;
    private Map<Class, Object> proxies = new HashMap<>();
    private Conf conf;
    private volatile boolean initialized = false;
    private ServiceBeanProvider serviceBeanProvider;

    /**
     *
     * @param confPath 配置文件路径
     * @param serviceBeanProvider 服务方bean的提供者
     */
    public RpcContext(String confPath, ServiceBeanProvider serviceBeanProvider) {
        this.confPath = confPath;
        this.serviceBeanProvider = serviceBeanProvider;
    }

    /**
     * 初始化
     * @throws Exception
     */
    public void init() throws Exception{
        parseConf(confPath);

        initClients();
        initServer();

        initialized = true;
    }

    private void initServer() throws Exception {
        if (conf.getServerConf() == null) {
            return;
        }

        Server server = genServer();
        server.init();
        server.start();

        registerServiceProvider();
    }

    private void registerServiceProvider() throws Exception {
        Register register = new ZooKeeperRegister();
        register.register(conf.getCommonConf(), conf.getServerConf());
    }

    private Server genServer(){
        if(conf.getServerConf().getProtocol() == Protocol.JSON){
            return new HttpServer(conf.getServerConf());
        }

        return null;
    }

    private void initClients() throws ClassNotFoundException {
        List<ClientConf> clients = conf.getClientConfs();
        for (ClientConf clientConf : clients) {
            Client client = new Client(conf.getCommonConf(), clientConf);
            client.init();
            Map<Class, Object> clientProxies = client.getProxies();
            clientProxies.keySet().stream().forEach(key -> proxies.put(key, clientProxies.get(key)));
        }

    }

    public <T> T getProxy(Class<T> clazz) {
        if (!initialized) {
            throw new RuntimeException("not initialized");
        }
        return (T) proxies.get(clazz);
    }

    private void parseConf(String confPath) throws Exception {
        conf = ConfParser.parseByPath(confPath);
        if(conf.getServerConf() != null){
            conf.getServerConf().setServiceBeanProvider(serviceBeanProvider);
        }
    }
}
