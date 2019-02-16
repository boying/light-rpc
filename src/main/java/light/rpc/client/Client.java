package light.rpc.client;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import light.rpc.conf.Config;
import light.rpc.exception.CircuitBreakerException;
import light.rpc.exception.ClientException;
import light.rpc.protocol.Request;
import light.rpc.server_address_provider.IServerAddressProvider;
import light.rpc.server_address_provider.ListedServerAddressProvider;
import light.rpc.server_address_provider.ZooKeeperServerAddressProvider;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户端类
 * 客户端是一个rpc服务方的客户端,可以获取调用代理对象,进行异步调用
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private Config config;

    private Config.Client clientConf;

    private Config.Registry registry;


    private IServerAddressProvider serverProvider;
    private CloseableHttpClient httpClient;
    private RequestConfig defaultRequestConfig;
    private Map<Class, Object> classObjMap = new HashMap<>();
    private Map<Method, Integer> methodTimeoutMap = new HashMap<>();

    private volatile boolean started = false;
    private static final int DEFAULT_METHOD_TIMEOUT = 10000;
    private static final int POOL_SIZE = 100;

    public Client(Config config, Config.Client clientConf) {
        this.config = config;
        this.clientConf = clientConf;
        this.registry = config.getRegistry();
    }

    /**
     * 启动客户端
     *
     * @throws ClassNotFoundException
     */
    public void init() throws ClassNotFoundException {
        if (started) {
            return;
        }

        initMethodTimeout();
        initServerProvider();
        initHttpClient();
        genProxies();
        started = true;
    }

    private void initHttpClient() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(POOL_SIZE);
        connManager.setDefaultMaxPerRoute(POOL_SIZE);
        connManager.setValidateAfterInactivity(60000);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .build();
        connManager.setDefaultSocketConfig(socketConfig);

        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .build();
        connManager.setDefaultConnectionConfig(connectionConfig);

        defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(1000)
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .build();

        this.httpClient = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(defaultRequestConfig).build();
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


    private void initMethodTimeout() {
        int defaultTimeout = Optional.ofNullable(clientConf.getMethodDefaultTimeoutMillisecond())
                .orElse(DEFAULT_METHOD_TIMEOUT);
        for (Config.Interface interfaceConf : clientConf.getInterfaces()) {
            Class clazz = interfaceConf.getClazz();
            Method[] methods = clazz.getMethods();

            methodTimeoutMap.putAll(Arrays.stream(methods)
                    .collect(Collectors.toMap(Function.identity(), i -> defaultTimeout)));
            methodTimeoutMap.putAll(interfaceConf.getMethods().stream()
                    .filter(i -> i.getTimeoutMillisecond() != null)
                    .collect(Collectors.toMap(Config.Method::getMethod, Config.Method::getTimeoutMillisecond)));
        }
    }

    private void initServerProvider() {
        if (clientConf.getServerProviders() != null && clientConf.getServerProviders().size() > 0) {
            serverProvider = new ListedServerAddressProvider(clientConf.getServerProviders());
        } else if (registry != null && registry.getAddress() != null) {
            ZooKeeperServerAddressProvider zooKeeperServerProvider = new ZooKeeperServerAddressProvider(registry.getAddress(), clientConf.getAppId());
            zooKeeperServerProvider.init();
            serverProvider = zooKeeperServerProvider;
        } else {
            throw new RuntimeException("no server provider conf");
        }
    }

    private void genProxies() throws ClassNotFoundException {
        classObjMap.putAll(clientConf.getInterfaces().stream().collect(Collectors
                .toMap(Config.Interface::getClazz, i -> genProxy(i.getClazz(), i))));
    }

    private Object genProxy(Class iface, Config.Interface conf) {
        return Proxy.newProxyInstance(Client.class.getClassLoader(), new Class[]{iface}, new Handler(iface));
    }

    /**
     * 代理对象的handler
     */
    private class Handler implements InvocationHandler {
        private Class<?> proxyClass;

        public Handler(Class<?> proxyClass) {
            this.proxyClass = proxyClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(proxyClass, args);
            }
            CommandResult result = new CallMethodCommand(method, args).execute();
            if (result.getException() == null) {
                return result.getResult();
            } else {
                throw result.getException();
            }
        }
    }

    @Data
    private static class CommandResult {
        public CommandResult(Object result, Exception exception) {
            this.result = result;
            this.exception = exception;
        }

        private Object result;
        private Exception exception;
    }

    private class CallMethodCommand extends HystrixCommand<CommandResult> {
        private Method method;
        private Object[] args;
        private Exception exception = null;

        public CallMethodCommand(Method method, Object[] args) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("" + Client.this.hashCode()))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(method.getName()))
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            //至少有n个请求，熔断器才进行错误率的计算
                            .withCircuitBreakerRequestVolumeThreshold(config.getCircuitBreaker().getRequestVolumeThreshold())
                            //熔断器中断请求n秒后会进入半打开状态,放部分流量过去重试
                            .withCircuitBreakerSleepWindowInMilliseconds(config.getCircuitBreaker().getSleepWindowInMilliseconds())
                            //错误率达到n开启熔断保护
                            .withCircuitBreakerErrorThresholdPercentage(config.getCircuitBreaker().getErrorThresholdPercentage())
                            .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                            .withExecutionTimeoutEnabled(false)
                            .withExecutionIsolationSemaphoreMaxConcurrentRequests(10)));

            this.method = method;
            this.args = args;
        }

        @Override
        protected CommandResult run() {
            try {
                return new CommandResult(invokeMethod(method, args), null);
            } catch (Exception e) {
                exception = new ClientException(e);
                throw e;
            }
        }


        @Override
        protected CommandResult getFallback() {
            return new CommandResult(null, exception == null ? new CircuitBreakerException() : exception);
        }
    }

    private Object invokeMethod(Method method, Object[] args) throws ClientException {
        try {
            return getResult(method, args);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    private Object getResult(Method method, Object[] args) throws IOException, ClassNotFoundException {
        Request request = RequestFactory.newRequest(method, args);
        String body = Request2JsonSerializer.serialize(request);
        logger.debug("request is {}", body);

        InetSocketAddress serverProviderAddress = this.serverProvider.get();

        HttpPost post = new HttpPost("http://" + serverProviderAddress.getHostName() + ":" + serverProviderAddress.getPort());

        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        post.setConfig(RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(methodTimeoutMap.get(method))
                .build());
        logger.debug("post req is {}", post);


        String rsp;
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            logger.debug("rsp is {}", response);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(String.format("response code is %s, not 200", response.getStatusLine().getStatusCode())); // TODO
            }

            rsp = EntityUtils.toString(response.getEntity());
        }
        // post.releaseConnection(); // TOOD ??

        logger.debug("response is {}", rsp);
        if (StringUtils.isEmpty(rsp)) {
            return null;
        } else {
            return Json2ResultDeserializer.deserialize(rsp, method.getReturnType());
        }
    }


}
