package light.rpc.conf;

import light.rpc.core.ServiceBeanProvider;
import lombok.Data;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class Config {
    private Registry registry;
    private List<Client> clients;
    private Server server;
    private RawConfig rawConfig;

    @Data
    public static class Registry {
        private InetSocketAddress address;
    }

    @Data
    public static class Client {
        private String appId;
        private Protocol protocol;
        private int threadPoolSize;
        private Integer methodDefaultTimeoutMillisecond;
        private List<InetSocketAddress> serverProviders;
        private List<Interface> interfaces;
    }

    @Data
    public static class Server {
        private String appId;
        private Protocol protocol;
        private int port;
        private ServiceBeanProvider serviceBeanProvider;
        private List<Class<?>> interfaces;
        private int threadPoolSize;
    }

    @Data
    public static class Interface {
        private Class clazz;
        private List<Method> methods;

    }

    @Data
    public static class Method {
        private java.lang.reflect.Method method;
        private Integer timeoutMillisecond;
    }
}
