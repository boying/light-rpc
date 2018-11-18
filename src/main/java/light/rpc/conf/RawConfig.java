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

    @Data
    public static class Registry{
        private String address;
    }

    @Data
    public static class Server {
        private String appId;
        private String protocol;
        private Integer port;
        private List<String> interfaces;
        private Integer threadPoolSize;
    }

    @Data
    public static class Client {
        private String appId;
        private String protocol;
        private Integer connectionPoolSize;
        private Integer methodDefaultTimeoutMillisecond;
        private List<IpPort> serverProviders;
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
}
