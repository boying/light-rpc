package core;

import client.async.AsyncCallFutureContainer;
import client.async.AsyncCallServer;
import client.Client;
import conf.ClientConf;
import conf.Conf;
import conf.ConfParser;
import conf.Protocol;
import conf.bean.Method;
import register.Register;
import register.ZooKeeperRegister;
import server.HttpServer;
import server.Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
public class RpcContext {
    private String confPath;
    private Map<Class, Object> classProxyMap = new HashMap<>();
    private Map<Object, Client> proxyClientMap = new HashMap<>();
    private Conf conf;
    private volatile boolean initialized = false;
    private ServiceBeanProvider serviceBeanProvider;
    private AsyncCallFutureContainer asyncCallFutureContainer = new AsyncCallFutureContainer();

    /**
     * @param confPath            配置文件路径
     * @param serviceBeanProvider 服务方bean的提供者
     */
    public RpcContext(String confPath, ServiceBeanProvider serviceBeanProvider) {
        this.confPath = confPath;
        this.serviceBeanProvider = serviceBeanProvider;
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    public void init() throws Exception {
        parseConf(confPath);

        initCommon();
        initClients();
        initServer();

        initialized = true;
    }

    private void initCommon(){
        conf.getCommonConf().setAsyncCallFutureContainer(asyncCallFutureContainer);
        initAsyncCallServer();
    }

    private void initAsyncCallServer(){
        AsyncCallServer asyncCallServer = new AsyncCallServer(conf.getCommonConf().getAsyncClientPort(), asyncCallFutureContainer);
        asyncCallServer.init();
        asyncCallServer.start();
    }

    public <T> Future<T> asyncCall(Object proxy, java.lang.reflect.Method method, Object[] args, Class<T> retType) {

        return proxyClientMap.get(proxy).asyncCall(method, args, retType); // TODO remove retType
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

    private Server genServer() {
        if (conf.getServerConf().getProtocol() == Protocol.JSON) {
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
            clientProxies.keySet().stream().forEach(key -> classProxyMap.put(key, clientProxies.get(key)));
            // TODO
            //clientProxies.values().stream().forEach(key -> proxyClientMap.put(key, client));
        }

    }

    public <T> T getProxy(Class<T> clazz) {
        if (!initialized) {
            throw new RuntimeException("not initialized");
        }
        return (T) classProxyMap.get(clazz);
    }

    private void parseConf(String confPath) throws Exception {
        conf = ConfParser.parseByPath(confPath);
        if (conf.getServerConf() != null) {
            conf.getServerConf().setServiceBeanProvider(serviceBeanProvider);
        }
    }
}
