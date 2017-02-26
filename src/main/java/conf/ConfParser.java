package conf;

import conf.bean.*;
import util.ClassUtil;
import util.InetSocketAddressFactory;
import util.json.JacksonHelper;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/19.
 */
public class ConfParser {
    public static Conf parseByPath(String path) throws Exception {
        Path p;
        if (path.startsWith("/")) {
            p = FileSystems.getDefault().getPath(path);
        } else {
            URL resource = ConfParser.class.getClassLoader().getResource(path);
            p = FileSystems.getDefault().getPath(resource.getPath());
        }
        String jsonStr = new String(Files.readAllBytes(p));
        return parse(jsonStr);
    }

    public static Conf parse(String jsonStr) throws Exception {
        conf.bean.Conf conf = JacksonHelper.getMapper().readValue(jsonStr, conf.bean.Conf.class);

        Conf ret = new Conf();
        ret.setCommonConf(parseCommonConf(conf.getCommon()));
        ret.setClientConfs(parseClientConfs(conf.getClients()));
        ret.setServerConf(parseServerConf(conf.getServer()));

        return ret;
    }

    private static ServerConf parseServerConf(Server server) throws ClassNotFoundException {
        if(server == null){
            return null;
        }

        ServerConf ret = new ServerConf();
        ret.setAppId(server.getAppId());
        ret.setProtocol(parseProtocol(server.getProtocol()));
        ret.setPort(server.getPort());
        ret.setThreadPoolSize(server.getThreadPoolSize());

        List<Class<?>> interfaces = new ArrayList<>();
        ret.setInterfaces(interfaces);
        if(server.getInterfaces() != null){
            for (String s : server.getInterfaces()) {
                interfaces.add(ClassUtil.forName(s));
            }
        }

        return ret;
    }

    private static List<ClientConf> parseClientConfs(List<Client> clients) throws ClassNotFoundException, NoSuchMethodException {
        if (clients == null || clients.size() == 0) {
            return Collections.emptyList();
        }

        List<ClientConf> ret = new ArrayList<>();
        for (Client client : clients) {
            ret.add(parseClientConf(client));
        }

        return ret;
    }

    private static ClientConf parseClientConf(Client client) throws ClassNotFoundException, NoSuchMethodException {
        if (client == null) {
            throw new IllegalArgumentException("client is null");
        }

        ClientConf ret = new ClientConf();
        ret.setAppId(client.getAppId());
        ret.setThreadPoolSize(client.getThreadPoolSize());
        ret.setMethodDefaultTimeoutMillisecond(client.getMethodDefaultTimeoutMillisecond());
        List<InterfaceConf> interfaceConfs = new ArrayList<>();
        ret.setProtocol(parseProtocol(client.getProtocol()));
        ret.setInterfaces(interfaceConfs);
        List<InetSocketAddress> serverProviders = new ArrayList<>();
        ret.setServerProviders(serverProviders);

        for (Interface anInterface : client.getInterfaces()) {
            InterfaceConf interfaceConf = new InterfaceConf();
            interfaceConfs.add(interfaceConf);

            interfaceConf.setClazz(ClassUtil.forName(anInterface.getName()));
            List<MethodConf> methodConfs = new ArrayList<>();
            interfaceConf.setMethodConfs(methodConfs);

            for (Method method : anInterface.getMethods()) {
                MethodConf methodConf = new MethodConf();
                methodConfs.add(methodConf);

                Class clazz = interfaceConf.getClazz();
                List<Class<?>> types = new ArrayList<>();
                for (String type : method.getParamTypes()) {
                    types.add(ClassUtil.forName(type));
                }

                java.lang.reflect.Method method1 = clazz.getMethod(method.getName(), Arrays.copyOf(types.toArray(), types.size(), Class[].class));
                methodConf.setMethod(method1);
                method.setTimeoutMillisecond(method.getTimeoutMillisecond());
            }
        }

        if(client.getServerProviders() != null){
            for (IpPort ipPort : client.getServerProviders()) {
                serverProviders.add(InetSocketAddressFactory.get(ipPort.getIp(), ipPort.getPort()));
            }
        }

        return ret;
    }

    private static Protocol parseProtocol(String protocol) {
        if ("json".equals(protocol)) {
            return Protocol.JSON;
        }
        throw new RuntimeException("invalid protocol " + protocol);
    }

    private static CommonConf parseCommonConf(Common common) {
        if (common == null) {
            throw new RuntimeException("common conf is null");
        }

        CommonConf ret = new CommonConf();
        ret.setRegistryAddress(InetSocketAddressFactory.get(common.getRegistryAddress()));
        ret.setAsyncClientPort(common.getAsyncClientPort());
        return ret;
    }


}
