package util;


import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangzhiwen on 17/2/13.
 */
public class HttpClientProvider {
    private static final int POOL_SIZE = 100;
    private static final int IDLE_TIMEOUT_MILLISECONDS = 60000;
    private static final int CLEAN_LOOP_INTERVAL_MILLISECONDS = 30000;
    private static final Map<InetSocketAddress, CloseableHttpClient> addressClientMap = new ConcurrentHashMap<>();
    static {
        new Thread(()->{
            while (true){
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

        }).start();
    }


    public static HttpClient getHttpClient(InetSocketAddress address){
        CloseableHttpClient httpClient = addressClientMap.get(address);
        if(httpClient == null){
            httpClient = genHttpClient(address);
            addressClientMap.putIfAbsent(address, httpClient);
        }
        return httpClient;
    }

    private static CloseableHttpClient genHttpClient(InetSocketAddress address){
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(POOL_SIZE);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost(address.getAddress(), address.getPort())), POOL_SIZE);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

}
