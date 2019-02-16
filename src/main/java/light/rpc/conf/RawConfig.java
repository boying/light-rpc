package light.rpc.conf;

import lombok.Data;

import java.util.List;

/**
 * Created by boying on 2018/11/9.
 */
@Data
public class RawConfig {
    private Registry registry;
    private Server server;
    private List<Client> clients;
    private CircuitBreaker circuitBreaker;

    @Data
    public static class Registry{
        private String address;
    }

    @Data
    public static class Server {
        private String appId;
        private Integer port;
        private String basePackage;
        private List<String> interfaces;
        private Integer threadPoolSize;
    }

    @Data
    public static class Client {
        private String appId;
        private Integer methodDefaultTimeoutMillisecond;
        private List<IpPort> serverProviders;
        private String basePackage;
        private List<Interface> interfaces;
    }

    @Data
    public static class IpPort {
        private String ip;
        private Integer port;
    }

    @Data
    public static class Method {
        private String name;
        private Integer timeoutMillisecond;
    }

    @Data
    public static class Interface {
        private String name;
        private List<Method> methods;
        private Integer timeoutMillisecond;
    }

    @Data
    public static class CircuitBreaker{
        private Integer requestVolumeThreshold;
        private Integer sleepWindowInMilliseconds;
        private Integer errorThresholdPercentage;
    }
}
