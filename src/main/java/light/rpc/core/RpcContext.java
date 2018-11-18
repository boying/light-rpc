package light.rpc.core;

import light.rpc.client.Client;
import light.rpc.conf.ConfParser;
import light.rpc.conf.Config;
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

/**
 * Rpc框架的执行环境,用于注册rpc服务,获取rpc调用者的代理对象
 */
public class RpcContext {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RpcContext.class);

    private String name; // TODO
    private String configPath;
    private Map<Class, Object> classProxyMap = new HashMap<>();
    private Map<Class, Client> classClientMap = new HashMap<>();
    private Config conf;
    private volatile boolean started = false;
    private ServiceBeanProvider serviceBeanProvider;
    private Server serviceServer;
    private List<Client> clients = new ArrayList<>();

    /**
     * @param confPath            配置文件路径
     * @param serviceBeanProvider 服务方bean的提供者
     */
    public RpcContext(String confPath, ServiceBeanProvider serviceBeanProvider) {
        this.configPath = confPath;
        this.serviceBeanProvider = serviceBeanProvider;
    }

    public RpcContext(Config conf) {
        this.conf = conf;
    }

    public RpcContext(String configPath) {
        this.configPath = configPath;
    }

    /**
     * 启动容器
     *
     * @throws Exception
     */
    public void start(boolean registerToRegistry) throws Exception {
        if (started) {
            throw new IllegalStateException("Rpc Context has been stated");
        }

        parseConf(configPath);

        initClients();
        startServer();
        if (registerToRegistry) {
            registerServiceToRegistry();
        }

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

        if (serviceServer != null) {
            Register register = new ZooKeeperRegister();
            try {
                register.unRegister(conf.getRegistry(), conf.getServer());
            } catch (Exception ignored) {
            }

            serviceServer.close();
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

    public Map<Class, Object> getClassProxyMap() {
        return classProxyMap;
    }


    private void parseConf(String confPath) throws Exception {
        conf = ConfParser.parseByPath(confPath);
        if (conf.getServer() != null) {
            conf.getServer().setServiceBeanProvider(serviceBeanProvider);
        }
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
        if (conf.getServer() == null) {
            return;
        }

        serviceServer = genServer();
        serviceServer.init();
        serviceServer.start();


    }

    public void registerServiceToRegistry() throws Exception {
        if (conf.getRegistry().getAddress() != null) {
            Register register = new ZooKeeperRegister();
            register.register(conf.getRegistry(), conf.getServer());
        }
    }

    private Server genServer() {
        return new HttpServer(conf.getServer());
    }

    private void initClients() throws ClassNotFoundException {
        List<Config.Client> clients = conf.getClients();
        for (Config.Client clientConf : clients) {
            Client client = new Client(conf.getRegistry(), clientConf);
            client.init();

            this.clients.add(client);
            Map<Class, Object> classProxyMap = client.getProxies();
            classProxyMap.keySet().stream().forEach(clazz -> this.classProxyMap.put(clazz, classProxyMap.get(clazz)));
            classProxyMap.keySet().stream().forEach(clazz -> classClientMap.put(clazz, client));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
