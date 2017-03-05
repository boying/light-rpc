package light.rpc.core;

import light.rpc.client.Client;
import light.rpc.client.async.AsyncCallFutureContainer;
import light.rpc.client.async.AsyncCallServer;
import light.rpc.conf.ClientConf;
import light.rpc.conf.Conf;
import light.rpc.conf.ConfParser;
import light.rpc.conf.Protocol;
import light.rpc.server.HttpServer;
import light.rpc.server.Server;
import light.rpc.service_register.Register;
import light.rpc.service_register.ZooKeeperRegister;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Rpc框架的执行环境,用于注册rpc服务,获取rpc调用者的代理对象
 */
public class RpcContext {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RpcContext.class);

    private String confPath;
    private Map<Class, Object> classProxyMap = new HashMap<>();
    private Map<Class, Client> classClientMap = new HashMap<>();
    private Conf conf;
    private volatile boolean started = false;
    private ServiceBeanProvider serviceBeanProvider;
    private AsyncCallFutureContainer asyncCallFutureContainer = new AsyncCallFutureContainer();
    private AsyncCallServer asyncCallServer;
    private Server serviceServer;
    private List<Client> clients = new ArrayList<>();

    /**
     * @param confPath            配置文件路径
     * @param serviceBeanProvider 服务方bean的提供者
     */
    public RpcContext(String confPath, ServiceBeanProvider serviceBeanProvider) {
        this.confPath = confPath;
        this.serviceBeanProvider = serviceBeanProvider;
    }

    /**
     * 启动容器
     *
     * @throws Exception
     */
    public void start() throws Exception {
        if (started) {
            throw new IllegalStateException("Rpc Context has been stated");
        }

        parseConf(confPath);

        initCommon();
        startClients();
        startServer();

        started = true;

        logger.debug("RpcContext started");
    }

    /**
     * 关闭容器
     */
    public void close() {
        if (!started) {
            return;
        }

        if (asyncCallServer != null) {
            asyncCallServer.close();
        }

        if (serviceServer != null) {
            Register register = new ZooKeeperRegister();
            try {
                register.unRegister(conf.getCommonConf(), conf.getServerConf());
            } catch (Exception ignored) {
            }

            serviceServer.close();
        }

        for (Client client : clients) {
            client.close();
        }

    }

    /**
     * 获取Rpc代理对象
     *
     * @param clazz 需要被代理的class
     * @param <T>   类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        if (!started) {
            throw new IllegalStateException("RpcContext not started");
        }
        return (T) classProxyMap.get(clazz);
    }

    /**
     * Rpc异步调用。适用于函数的入参为空,返回值为void的函数
     *
     * @param clazz      Rpc调用接口类
     * @param methodName 方法名
     * @return 调用结果Future
     */
    public Future<Void> asyncCall(Class<?> clazz, String methodName) {
        return asyncCall(clazz, methodName, new Class[]{}, new Object[]{}, Void.class);
    }

    /**
     * Rpc异步调用。适用于函数的入参为空的函数
     *
     * @param clazz      Rpc调用接口类
     * @param methodName 方法名
     * @param retType    返回值类
     * @param <T>        返回值类型
     * @return 调用结果Future
     */
    public <T> Future<T> asyncCall(Class<?> clazz, String methodName, Class<T> retType) {
        return asyncCall(clazz, methodName, new Class[]{}, new Object[]{}, retType);
    }

    /**
     * Rpc异步调用
     *
     * @param clazz      Rpc调用接口类
     * @param methodName 方法名
     * @param argTypes   参数类型列表
     * @param args       参数值
     * @param retType    返回值类
     * @param <T>        返回值类型
     * @return 调用结果Future
     */
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

    private void parseConf(String confPath) throws Exception {
        conf = ConfParser.parseByPath(confPath);
        if (conf.getServerConf() != null) {
            conf.getServerConf().setServiceBeanProvider(serviceBeanProvider);
        }
    }

    private void initCommon() {
        conf.getCommonConf().setAsyncCallFutureContainer(asyncCallFutureContainer);
        initAsyncCallServer();
    }

    private void initAsyncCallServer() {
        asyncCallServer = new AsyncCallServer(conf.getCommonConf().getAsyncClientPort(), asyncCallFutureContainer);
        asyncCallServer.init();
        asyncCallServer.start();
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

    private void startServer() throws Exception {
        if (conf.getServerConf() == null) {
            return;
        }

        serviceServer = genServer();
        serviceServer.init();
        serviceServer.start();

        registerServiceProvider();
    }

    private void registerServiceProvider() throws Exception {
        if (conf.getCommonConf().getRegistryAddress() != null) {
            Register register = new ZooKeeperRegister();
            register.register(conf.getCommonConf(), conf.getServerConf());
        }
    }

    private Server genServer() {
        if (conf.getServerConf().getProtocol() == Protocol.JSON) {
            return new HttpServer(conf.getServerConf());
        }

        return null;
    }

    private void startClients() throws ClassNotFoundException {
        List<ClientConf> clients = conf.getClientConfs();
        for (ClientConf clientConf : clients) {
            Client client = new Client(conf.getCommonConf(), clientConf);
            client.start();

            this.clients.add(client);
            Map<Class, Object> classProxyMap = client.getProxies();
            classProxyMap.keySet().stream().forEach(clazz -> this.classProxyMap.put(clazz, classProxyMap.get(clazz)));
            classProxyMap.keySet().stream().forEach(clazz -> classClientMap.put(clazz, client));
        }
    }

}
