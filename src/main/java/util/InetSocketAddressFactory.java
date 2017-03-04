package util;

import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/19.
 */
public class InetSocketAddressFactory {
    public static InetSocketAddress get(String host, int port) {
        if (host == null) {
            throw new IllegalArgumentException("host is null");
        }
        return new InetSocketAddress(host, port);
    }

    public static InetSocketAddress get(String ipPort) {
        if (ipPort == null) {
            throw new IllegalArgumentException("host is null");
        }

        String[] splits = ipPort.split(":");
        if (splits.length != 2) {
            throw new IllegalArgumentException("invalid hostIp[" + ipPort + "] format");
        }

        int port;
        try {
            port = Integer.parseInt(splits[1]);

        } catch (Exception e) {
            throw new IllegalArgumentException("invalid hostIp[" + ipPort + "] format");
        }

        return new InetSocketAddress(splits[0], port);
    }
}
