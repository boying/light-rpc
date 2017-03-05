package light.rpc.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import light.rpc.client.async.AsyncCallTask;
import light.rpc.client.sync.SyncCallTask;
import light.rpc.conf.*;
import light.rpc.exception.ClientException;
import light.rpc.exception.ClientTaskRejectedException;
import light.rpc.exception.ClientTimeoutException;
import light.rpc.exception.ServerException;
import light.rpc.result.Result;
import light.rpc.server_address_provider.IServerAddressProvider;
import light.rpc.server_address_provider.ListedServerAddressProvider;
import light.rpc.server_address_provider.ZooKeeperServerAddressProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 客户端类
 * 客户端是一个rpc服务方的客户端,可以获取调用代理对象,进行异步调用
 */
@RequiredArgsConstructor
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    /**
     * 通用参数
     */
    @NonNull
    private final CommonConf commonConf;

    /**
     * 客户端参数
     */
    @NonNull
    private final ClientConf clientConf;

    private IServerAddressProvider serverProvider;
    private Map<Class, Object> classObjMap = new HashMap<>();
    private ExecutorService threadPool;
    private Map<Method, Integer> methodTimeoutMap = new HashMap<>();
    private volatile boolean started = false;
    private static final int DEFAULT_METHOD_TIMEOUT = 10000;

    /**
     * 启动客户端
     *
     * @throws ClassNotFoundException
     */
    public void start() throws ClassNotFoundException {
        if (started) {
            return;
        }

        initThreadPool();
        initMethodTimeout();
        initServerProvider();
        genProxies();
        started = true;
    }

    /**
     * 关闭客户端
     */
    public void close() {
        if (!started) {
            return;
        }

        threadPool.shutdown();
    }

    /**
     * 获取rpc调用者代理对象
     *
     * @return key是rpc接口类, value是rpc接口代理对象
     */
    public Map<Class, Object> getProxies() {
        if (!started) {
            throw new IllegalStateException("client no started");
        }
        return this.classObjMap;
    }

    /**
     * 异步调用
     *
     * @param clazz   异步调用目标类
     * @param method  异步调用的方法
     * @param args    方法参数值
     * @param retType 返回值类型
     * @param <T>
     * @return
     */
    public <T> Future<T> asyncCall(Class clazz, Method method, Object[] args, Class<T> retType) {
        if (!started) {
            throw new IllegalStateException("client no started");
        }
        try {
            return new AsyncCallTask<T>(clazz, method, args, serverProvider, commonConf.getAsyncCallFutureContainer(), commonConf.getAsyncClientPort()).getFuture();
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    private void initThreadPool() {
        String threadNameFormat = "rcp_client-" + clientConf.getAppId() + "-thread_pool-thread-%d";
        threadPool = new ThreadPoolExecutor(0, clientConf.getThreadPoolSize(), 1, TimeUnit.MINUTES,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build());
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
            serverProvider = new ListedServerAddressProvider(clientConf.getServerProviders());
        } else if (commonConf != null && commonConf.getRegistryAddress() != null) {
            ZooKeeperServerAddressProvider zooKeeperServerProvider = new ZooKeeperServerAddressProvider(commonConf.getRegistryAddress(), clientConf.getAppId());
            zooKeeperServerProvider.init();
            serverProvider = zooKeeperServerProvider;
        } else {
            throw new RuntimeException("no server provider conf");
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
        return Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[]{iface}, new Handler(iface, clientConf.getProtocol()));
    }

    /**
     * 代理对象的handler
     */
    private class Handler implements InvocationHandler {
        private Class<?> proxyClass;
        private Protocol protocol;

        public Handler(Class<?> proxyClass, Protocol protocol) {
            this.proxyClass = proxyClass;
            this.protocol = protocol;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(proxyClass, args);
            }
            int methodTimeout = methodTimeoutMap.get(method);
            Future<Result> resultFuture;
            long start = System.currentTimeMillis();
            try {
                resultFuture = threadPool.submit(newTask(method, args));
            } catch (RejectedExecutionException e) {
                logger.warn("client thread pool full");
                throw new ClientTaskRejectedException("client thread pool full");
            }
            Result result;
            try {
                result = resultFuture.get(methodTimeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new ClientTimeoutException();
            } catch (Exception e) {
                throw new ClientException(e);
            } finally {
                logger.debug("cost " + (System.currentTimeMillis() - start) + " mills");
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
                return new SyncCallTask(serverProvider, method, args);
            }
            return null;
        }
    }

}
