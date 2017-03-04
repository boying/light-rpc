import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import util.CloseableHttpClientFactory;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by jiangzhiwen on 17/2/14.
 */
public class HttpClientFactoryTest {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            scanner.nextLine();

            for(int i = 0; i < 20; ++i){
                new Thread(()->{
                    try {
                        for(int x = 0; x < 20; ++x) {
                            long start = System.currentTimeMillis();
                            f();
                            long end = System.currentTimeMillis();
                            System.out.println("cost: " + (end - start) + " " + Thread.currentThread().getId());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }


        }
    }

    public static void main3(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            scanner.nextLine();

            long start = System.currentTimeMillis();
            f();

            long end = System.currentTimeMillis();

            System.out.println("cost: " + (end - start));
        }
    }

    public static void f() throws IOException {
        //System.out.println("begin");
        CloseableHttpClient httpClient = (CloseableHttpClient) CloseableHttpClientFactory.getCloseableHttpClient("192.168.112.35:80");
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        //PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //HttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        HttpGet get = new HttpGet("http://bpm-eve.alpha.elenet.me/coffee-eve-svr");
        CloseableHttpResponse response = httpClient.execute(get);
        //System.out.println(EntityUtils.toString(response.getEntity()));
        response.close();
        //response.close();
        //((CloseableHttpClient)gClient).close();

        //System.out.println("end");
    }

    public static void main2(String[] args) throws IOException {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        int POOL_SIZE = 100;
        cm.setMaxTotal(POOL_SIZE);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost("127.0.0.1", 8080)), POOL_SIZE);
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build();

        Scanner sc = new Scanner(System.in);

        while (true) {
            sc.nextLine();

            HttpGet httpGet = new HttpGet("http://localhost:8080/");
            CloseableHttpResponse execute = client.execute(httpGet);
            String s = EntityUtils.toString(execute.getEntity());
            System.out.println(s);
        }

    }
}
