package client;

import conf.ClientConf;
import conf.CommonConf;
import conf.InterfaceConf;
import conf.Protocol;
import server_provider.IServerProvider;
import server_provider.ListedServerProvider;
import server_provider.ZooKeeperServerProvider;
import task.JsonTask;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
public class Client {
    private CommonConf commonConf;
    private ClientConf clientConf;
    private IServerProvider serverProvider;
    private Map<Class, Object> classObjMap = new HashMap<>();
    private ExecutorService threadPool;

    public Client(CommonConf commonConf, ClientConf clientConf) {
        this.commonConf = commonConf;
        this.clientConf = clientConf;
    }

    public void init() throws ClassNotFoundException {
        threadPool = new ThreadPoolExecutor(0, clientConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(0));

        initServerProvider();
        genProxies();
    }

    private void initServerProvider() {
        if(clientConf.getServerProviders() != null || clientConf.getServerProviders().size() > 0){
            serverProvider = new ListedServerProvider(clientConf.getServerProviders());
        }else if(commonConf != null && commonConf.getRegistryAddress() != null){
            serverProvider = new ZooKeeperServerProvider(commonConf.getRegistryAddress(), clientConf.getAppId());
        }else{
            throw new RuntimeException("No Server Provider conf");
        }
    }

    private void genProxies() throws ClassNotFoundException {
        List<InterfaceConf> interfaces = clientConf.getInterfaces();
        for (InterfaceConf ifaceConf : interfaces) {
            Class<?> clazz = ifaceConf.getClass();
            classObjMap.put(clazz, genProxy(clazz, ifaceConf));
        }
    }

    private Object genProxy(Class iface, InterfaceConf conf) {
        return Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[]{iface}, new Handler(clientConf.getProtocol()));
    }

    private class Handler implements InvocationHandler {
        private Protocol protocol;

        public Handler(Protocol protocol) {
            this.protocol = protocol;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            int methodTimeout = 0;
            Future<Object> f = threadPool.submit(newTask(method, args));
            Object ret = f.get(methodTimeout, TimeUnit.MILLISECONDS);
            return ret;
        }

        private Callable newTask(Method method, Object[] args) {
            if (protocol == Protocol.JSON) {
                return new JsonTask(serverProvider, method, args);
            }
            return null;
        }
    }

    public Map<Class, Object> getProxies() {
        return this.classObjMap;
    }
}
