import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by jiangzhiwen on 17/2/14.
 */
public class HttpClientProviderTest {
    public static void main(String[] args) throws IOException {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        int POOL_SIZE = 100;
        cm.setMaxTotal(POOL_SIZE);
        cm.setMaxPerRoute(new HttpRoute(new HttpHost("127.0.0.1", 8080)), POOL_SIZE);
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(cm).build();

        Scanner sc = new Scanner(System.in);

        while(true) {
            sc.nextLine();

            HttpGet httpGet = new HttpGet("http://localhost:8080/");
            CloseableHttpResponse execute = client.execute(httpGet);
            String s = EntityUtils.toString(execute.getEntity());
            System.out.println(s);
        }

    }
}
