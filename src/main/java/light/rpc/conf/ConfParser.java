package light.rpc.conf;

import light.rpc.util.ClassUtil;
import light.rpc.util.InetSocketAddressFactory;
import light.rpc.util.PackageUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 配置文件解析器
 */
public class ConfParser {
    /**
     * 根据文件名解析配置
     *
     * @param path 文件路径。如果文件路径以/打头,将从系统文件路径加载;如果是相对路径,则从classpath查找
     */
    public static Config parseByPath(String path) {
        Path p;
        if (path.startsWith("/")) {
            p = FileSystems.getDefault().getPath(path);
        } else {
            try {
                p = Paths.get(ConfParser.class.getClassLoader().getResource(path).toURI());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        String content;
        try {
            content = new String(Files.readAllBytes(p));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        RawConfig rawConfig = parseRawConfigFromYaml(content);
        return parse(rawConfig);
    }

    private static RawConfig parseRawConfigFromYaml(String str) {
        return new Yaml(new Constructor(RawConfig.class)).load(str);
    }


    public static Config parse(RawConfig rawConfig) {
        Config ret = new Config();
        ret.setRegistry(parseRegistry(rawConfig.getRegistry()));
        ret.setClients(parseClients(rawConfig.getClients()));
        ret.setServer(parseServer(rawConfig.getServer()));
        ret.setCircuitBreaker(parseCircuitBreaker(rawConfig.getCircuitBreaker()));
        ret.setRawConfig(rawConfig);

        return ret;
    }

    private static Config.CircuitBreaker parseCircuitBreaker(RawConfig.CircuitBreaker circuitBreaker) {
        if (circuitBreaker == null) {
            circuitBreaker = new RawConfig.CircuitBreaker();
        }
        Config.CircuitBreaker ret = new Config.CircuitBreaker();
        ret.setErrorThresholdPercentage(Optional.ofNullable(circuitBreaker.getErrorThresholdPercentage()).orElse(50));
        ret.setRequestVolumeThreshold(Optional.ofNullable(circuitBreaker.getRequestVolumeThreshold()).orElse(10));
        ret.setSleepWindowInMilliseconds(Optional.ofNullable(circuitBreaker.getSleepWindowInMilliseconds()).orElse(5000));
        return ret;
    }

    private static Config.Server parseServer(RawConfig.Server server) {
        if (server == null) {
            return null;
        }

        Config.Server ret = new Config.Server();
        ret.setAppId(server.getAppId());
        ret.setPort(server.getPort());
        if (server.getThreadPoolSize() != null) {
            ret.setThreadPoolSize(server.getThreadPoolSize());
        }

        Set<Class> classSet = StringUtils.isBlank(server.getBasePackage()) ?
                Collections.emptySet() : PackageUtils.findInterfaces(server.getBasePackage());
        List<Class> interfaces = new ArrayList<>(classSet);
        ret.setInterfaces(interfaces);
        if (server.getInterfaces() != null) {
            for (String s : server.getInterfaces()) {
                Class<?> aClass = ClassUtil.forName(s);
                if (!classSet.contains(aClass)) {
                    interfaces.add(aClass);
                }
            }
        }

        return ret;
    }

    private static List<Config.Client> parseClients(List<RawConfig.Client> raws) {
        return Optional.ofNullable(raws).orElse(Collections.emptyList()).stream()
                .map(ConfParser::parseClient)
                .collect(Collectors.toList());
    }

    private static Config.Client parseClient(RawConfig.Client rawClient) {
        if (rawClient == null) {
            throw new IllegalArgumentException("light.rpc.rawClient is null");
        }

        Config.Client ret = new Config.Client();
        ret.setAppId(rawClient.getAppId());
        ret.setMethodDefaultTimeoutMillisecond(rawClient.getMethodDefaultTimeoutMillisecond());
        List<InetSocketAddress> serverProviders = new ArrayList<>();
        ret.setServerProviders(serverProviders);

        Set<Class> classSet = StringUtils.isBlank(rawClient.getBasePackage())
                ? Collections.emptySet() : PackageUtils.findInterfaces(rawClient.getBasePackage());
        ArrayList<Config.Interface> interfaces = classSet.stream()
                .map(i -> new Config.Interface(i, new ArrayList<>()))
                .collect(Collectors.toCollection(ArrayList::new));
        ret.setInterfaces(interfaces);

        Map<Class, Config.Interface> classToInterface = interfaces.stream()
                .collect(Collectors.toMap(Config.Interface::getClazz, Function.identity()));

        for (RawConfig.Interface anInterface : Optional.ofNullable(rawClient.getInterfaces())
                .orElse(Collections.emptyList())) {
            Class<?> aClass = ClassUtil.forName(anInterface.getName());
            Config.Interface iface;
            if (classSet.contains(aClass)) {
                iface = classToInterface.get(aClass);
            } else {
                iface = new Config.Interface(aClass, new ArrayList<>());
                interfaces.add(iface);
            }

            List<Config.Method> methods = iface.getMethods();

            methods.addAll(anInterface.getMethods().stream()
                    .map(i -> new Config.Method(getMethodByName(iface.getClazz(), i.getName()),
                            i.getTimeoutMillisecond()))
                    .collect(Collectors.toList()));
        }

        serverProviders.addAll(
                Optional.ofNullable(rawClient.getServerProviders()).orElse(Collections.emptyList()).stream()
                        .map(i -> InetSocketAddressFactory.get(i.getIp(), i.getPort()))
                        .collect(Collectors.toList()));

        return ret;
    }

    private static Method getMethodByName(Class clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalStateException("not find method " + methodName);
    }

    private static Config.Registry parseRegistry(RawConfig.Registry raw) {
        if (raw == null) {
            throw new RuntimeException("raw light.rpc.conf is null");
        }

        Config.Registry ret = new Config.Registry();
        if (raw.getAddress() != null) {
            ret.setAddress(InetSocketAddressFactory.get(raw.getAddress()));
        }
        return ret;
    }


}
