package client;

import bean.Result;
import conf.ClientConf;
import conf.CommonConf;
import conf.InterfaceConf;
import conf.Protocol;
import exception.ClientException;
import exception.ClientTimeoutException;
import exception.ServerException;
import server_provider.IServerProvider;
import server_provider.ListedServerProvider;
import server_provider.ZooKeeperServerProvider;

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
        threadPool = new ThreadPoolExecutor(0, clientConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1)); // TODO 1

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
        List<InterfaceConf> interfaceConfs = clientConf.getInterfaces();
        for (InterfaceConf interfaceConf : interfaceConfs) {
            Class<?> clazz = interfaceConf.getClazz();
            classObjMap.put(clazz, genProxy(clazz, interfaceConf));
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
            // TODO
            int methodTimeout = 1000000;
            Future<Result> resultFuture = threadPool.submit(newTask(method, args));
            Result result;
            try {
                result = resultFuture.get(methodTimeout, TimeUnit.MILLISECONDS);
            }catch (TimeoutException e){
                throw new ClientTimeoutException();
            }catch (Exception e){
                throw new ClientException(e);
            }

            if(result.isInvokedSuccess()){
                if(result.getThrowable() == null){
                    return result.getResult();
                }else{
                    throw result.getThrowable();
                }
            } else{
                throw new ServerException(result.getErrorMsg());
            }
        }

        private Callable newTask(Method method, Object[] args) {
            if (protocol == Protocol.JSON) {
                return new Task(serverProvider, method, args);
            }
            return null;
        }
    }

    public Map<Class, Object> getProxies() {
        return this.classObjMap;
    }
}
