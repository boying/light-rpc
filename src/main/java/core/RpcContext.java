package core;

import client.Client;
import client.async.AsyncCallFutureContainer;
import client.async.AsyncCallServer;
import conf.ClientConf;
import conf.Conf;
import conf.ConfParser;
import conf.Protocol;
import register.Register;
import register.ZooKeeperRegister;
import server.HttpServer;
import server.Server;

import java.lang.reflect.Method;
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
    private Map<Class, Client> classClientMap = new HashMap<>();
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

    private void initCommon() {
        conf.getCommonConf().setAsyncCallFutureContainer(asyncCallFutureContainer);
        initAsyncCallServer();
    }

    private void initAsyncCallServer() {
        AsyncCallServer asyncCallServer = new AsyncCallServer(conf.getCommonConf().getAsyncClientPort(), asyncCallFutureContainer);
        asyncCallServer.init();
        asyncCallServer.start();
    }

    public <T> Future<T> asyncCall(Class<?> clazz, String methodName, Class<?>[] argTypes, Object[] args, Class<T> retType) {
        if (args.length != argTypes.length) {
            throw new IllegalArgumentException("argTypes length not equal args length");
        }
        Method method = findMethod(clazz, methodName, argTypes);
        if (method == null) {
            throw new RuntimeException("method no found");
        }

        return classClientMap.get(clazz).asyncCall(clazz, method, args, retType);
    }

    private Method findMethod(Class clazz, String methodName, Class<?>[] argTypes) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != argTypes.length) {
                    return null;
                }
                for (int i = 0; i < parameterTypes.length; ++i) {
                    if (parameterTypes[i] != argTypes[i]) {
                        return null;
                    }
                }
                return method;
            }
        }
        return null;
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

            Map<Class, Object> classProxyMap = client.getProxies();
            classProxyMap.keySet().stream().forEach(clazz -> this.classProxyMap.put(clazz, classProxyMap.get(clazz)));
            classProxyMap.keySet().stream().forEach(clazz -> classClientMap.put(clazz, client));
        }

    }

    /**
     * 获取Rpc代理对象
     * @param clazz 需要被代理的class
     * @param <T>
     * @return
     */
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
