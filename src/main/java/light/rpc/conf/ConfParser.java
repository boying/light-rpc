package light.rpc.conf;

import light.rpc.util.ClassUtil;
import light.rpc.util.InetSocketAddressFactory;
import light.rpc.util.json.JacksonHelper;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 配置文件解析器
 */
public class ConfParser {
    /**
     * 根据文件名解析配置
     *
     * @param path 文件路径。如果文件路径以/打头,将从系统文件路径加载;如果是相对路径,则从classpath查找
     * @return
     * @throws Exception
     */
    public static Config parseByPath(String path) throws Exception {
        Path p;
        if (path.startsWith("/")) {
            p = FileSystems.getDefault().getPath(path);
        } else {
            p = Paths.get(ConfParser.class.getClassLoader().getResource(path).toURI());
        }
        String jsonStr = new String(Files.readAllBytes(p));
        return parse(jsonStr);
    }

    /**
     * 根据json串解析配置
     *
     * @param jsonStr
     * @return
     * @throws Exception
     */
    public static Config parse(String jsonStr) throws Exception {
        RawConfig config = JacksonHelper.getMapper().readValue(jsonStr, RawConfig.class);
        return parse(config);
    }

    public static Config parse(RawConfig rawConfig) throws NoSuchMethodException, ClassNotFoundException {
        Config ret = new Config();
        ret.setRegistry(parseRegistry(rawConfig.getRegistry()));
        ret.setClients(parseClients(rawConfig.getClients()));
        ret.setServer(parseServer(rawConfig.getServer()));
        ret.setRawConfig(rawConfig);

        return ret;
    }

    private static Config.Server parseServer(RawConfig.Server server) throws ClassNotFoundException {
        if (server == null) {
            return null;
        }

        Config.Server ret = new Config.Server();
        ret.setAppId(server.getAppId());
        ret.setProtocol(parseProtocol(server.getProtocol()));
        ret.setPort(server.getPort());
        ret.setThreadPoolSize(server.getThreadPoolSize());

        List<Class<?>> interfaces = new ArrayList<>();
        ret.setInterfaces(interfaces);
        if (server.getInterfaces() != null) {
            for (String s : server.getInterfaces()) {
                interfaces.add(ClassUtil.forName(s));
            }
        }

        return ret;
    }

    private static List<Config.Client> parseClients(List<RawConfig.Client> raws) throws ClassNotFoundException, NoSuchMethodException {
        if (raws == null || raws.size() == 0) {
            return Collections.emptyList();
        }

        List<Config.Client> ret = new ArrayList<>();
        for (RawConfig.Client client : raws) {
            ret.add(parseClient(client));
        }

        return ret;
    }

    private static Config.Client parseClient(RawConfig.Client rawClient) throws ClassNotFoundException, NoSuchMethodException {
        if (rawClient == null) {
            throw new IllegalArgumentException("light.rpc.rawClient is null");
        }

        Config.Client ret = new Config.Client();
        ret.setAppId(rawClient.getAppId());
        ret.setMethodDefaultTimeoutMillisecond(rawClient.getMethodDefaultTimeoutMillisecond());
        List<Config.Interface> interfaceConfs = new ArrayList<>();
        ret.setProtocol(parseProtocol(rawClient.getProtocol()));
        ret.setInterfaces(interfaceConfs);
        List<InetSocketAddress> serverProviders = new ArrayList<>();
        ret.setServerProviders(serverProviders);

        for (RawConfig.Interface anInterface : rawClient.getInterfaces()) {
            Config.Interface interfaceConf = new Config.Interface();
            interfaceConfs.add(interfaceConf);

            interfaceConf.setClazz(ClassUtil.forName(anInterface.getName()));
            List<Config.Method> methods = new ArrayList<>();
            interfaceConf.setMethods(methods);

            for (RawConfig.Method rawMethod : anInterface.getMethods()) {
                Config.Method method = new Config.Method();
                methods.add(method);

                Class clazz = interfaceConf.getClazz();

                method.setMethod(getMethodByName(clazz, rawMethod.getName()));
                method.setTimeoutMillisecond(rawMethod.getTimeoutMillisecond());
            }
        }

        if (rawClient.getServerProviders() != null) {
            for (RawConfig.IpPort ipPort : rawClient.getServerProviders()) {
                serverProviders.add(InetSocketAddressFactory.get(ipPort.getIp(), ipPort.getPort()));
            }
        }

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

    private static Protocol parseProtocol(String protocol) {
        if ("json".equals(protocol)) {
            return Protocol.JSON;
        }
        throw new RuntimeException("invalid protocol " + protocol);
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
