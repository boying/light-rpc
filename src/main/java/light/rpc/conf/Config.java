package light.rpc.conf;

import light.rpc.core.ServiceBeanProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by boying on 17/2/11.
 */
@Data
public class Config {
    private Registry registry;
    private List<Client> clients;
    private Server server;
    private CircuitBreaker circuitBreaker;
    private RawConfig rawConfig;

    @Data
    public static class Registry {
        private InetSocketAddress address;
    }

    @Data
    public static class Client {
        private String appId;
        private int threadPoolSize;
        private Integer methodDefaultTimeoutMillisecond;
        private List<InetSocketAddress> serverProviders;
        private List<Interface> interfaces;
    }

    @Data
    public static class Server {
        private String appId;
        private int port;
        private ServiceBeanProvider serviceBeanProvider;
        private List<Class> interfaces;
        private int threadPoolSize = Runtime.getRuntime().availableProcessors() * 2;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interface {
        private Class clazz;
        private List<Method> methods;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Method {
        private java.lang.reflect.Method method;
        private Integer timeoutMillisecond;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CircuitBreaker{
        private Integer requestVolumeThreshold;
        private Integer sleepWindowInMilliseconds;
        private Integer errorThresholdPercentage;
    }
}
