package light.rpc.util;


import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.net.InetSocketAddress;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by boying on 17/2/13.
 */
public class CloseableHttpClientFactory {
    private static final int POOL_SIZE = 100;
    private static final int IDLE_TIMEOUT_MILLISECONDS = 60000;
    private static final int CLEAN_LOOP_INTERVAL_MILLISECONDS = 30000;
    private static final Map<InetSocketAddress, CloseableHttpClient> addressClientMap = new ConcurrentHashMap<>();

    static {
        Thread t = new Thread(() -> {
            while (true) {
                for (CloseableHttpClient closeableHttpClient : addressClientMap.values()) {
                    ClientConnectionManager connectionManager = closeableHttpClient.getConnectionManager();
                    connectionManager.closeExpiredConnections();
                    connectionManager.closeIdleConnections(IDLE_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
                }

                try {
                    Thread.sleep(CLEAN_LOOP_INTERVAL_MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        t.setDaemon(true);
        t.start();
    }

    public static CloseableHttpClient getCloseableHttpClient(String ipPort) {
        return getCloseableHttpClient(InetSocketAddressFactory.get(ipPort));
    }

    public static CloseableHttpClient getCloseableHttpClient(String ip, int port) {
        return getCloseableHttpClient(InetSocketAddressFactory.get(ip, port));
    }

    public static CloseableHttpClient getCloseableHttpClient(InetSocketAddress address) {
        CloseableHttpClient httpClient = addressClientMap.get(address);
        if (httpClient == null) {
            synchronized (addressClientMap) {
                httpClient = addressClientMap.get(address);
                if (httpClient == null) {
                    httpClient = genCloseableHttpClient(address);
                    addressClientMap.putIfAbsent(address, httpClient);
                }
            }
        }
        return httpClient;
    }

    private static CloseableHttpClient genCloseableHttpClient(InetSocketAddress address) {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(POOL_SIZE);
        connManager.setDefaultMaxPerRoute(POOL_SIZE);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost(address.getAddress(), address.getPort())), POOL_SIZE);
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

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(1000)
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .build();

        return HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(defaultRequestConfig).build();
    }

}
