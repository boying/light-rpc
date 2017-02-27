package client;

import bean.Result;
import client.async.AsyncCallTask;
import conf.*;
import exception.ClientException;
import exception.ClientTaskRejectedException;
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
import java.util.Optional;
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
    private Map<Method, Integer> methodTimeoutMap = new HashMap<>();
    private static final int DEFAULT_METHOD_TIMEOUT = 10000;
    private static final int WAITING_QUEUE_SIZE = 1000;

    public Client(CommonConf commonConf, ClientConf clientConf) {
        this.commonConf = commonConf;
        this.clientConf = clientConf;
    }

    public void init() throws ClassNotFoundException {
        threadPool = new ThreadPoolExecutor(0, clientConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WAITING_QUEUE_SIZE));

        initMethodTimeout();
        initServerProvider();
        genProxies();
    }

    private void initMethodTimeout() {
        int defaultTimeout = Optional.ofNullable(clientConf.getMethodDefaultTimeoutMillisecond()).orElse(DEFAULT_METHOD_TIMEOUT);
        for (InterfaceConf interfaceConf : clientConf.getInterfaces()) {
            Class clazz = interfaceConf.getClazz();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                methodTimeoutMap.put(method, defaultTimeout);
            }

            for (MethodConf methodConf : interfaceConf.getMethodConfs()) {
                if (methodConf.getTimeoutMillisecond() != null) {
                    methodTimeoutMap.put(methodConf.getMethod(), methodConf.getTimeoutMillisecond());
                }
            }
        }
    }

    private void initServerProvider() {
        if (clientConf.getServerProviders() != null && clientConf.getServerProviders().size() > 0) {
            serverProvider = new ListedServerProvider(clientConf.getServerProviders());
        } else if (commonConf != null && commonConf.getRegistryAddress() != null) {
            ZooKeeperServerProvider zooKeeperServerProvider = new ZooKeeperServerProvider(commonConf.getRegistryAddress(), clientConf.getAppId());
            zooKeeperServerProvider.init();
            serverProvider = zooKeeperServerProvider;
        } else {
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
            int methodTimeout = methodTimeoutMap.get(method);
            // TODO
            methodTimeout = 100000;
            Future<Result> resultFuture;
            long start;
            try {
                start = System.currentTimeMillis();
                resultFuture = threadPool.submit(newTask(method, args));
            } catch (RejectedExecutionException e) {
                throw new ClientTaskRejectedException("client thread pool full");
            }
            Result result;
            try {
                result = resultFuture.get(methodTimeout, TimeUnit.MILLISECONDS);
                System.out.println("cost " + (System.currentTimeMillis() - start) + " mills");
            } catch (TimeoutException e) {
                throw new ClientTimeoutException();
            } catch (Exception e) {
                throw new ClientException(e);
            } finally {
                resultFuture.cancel(true);
            }

            if (result.isInvokedSuccess()) {
                if (result.getThrowable() == null) {
                    return result.getResult();
                } else {
                    throw result.getThrowable();
                }
            } else {
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

    public <T> Future<T> asyncCall(Method method, Object[] args, Class<T> retType) {

        // TODO generic
        return new AsyncCallTask<T>(method, args, serverProvider, commonConf.getAsyncCallFutureContainer(), commonConf.getAsyncClientPort()).getFuture();
    }
}
