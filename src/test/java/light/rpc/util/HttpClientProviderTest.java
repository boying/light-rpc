package light.rpc.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class HttpClientProviderTest {
    public static void main(String[] args) throws IOException {

        InetSocketAddress serverProviderAddress = InetSocketAddressFactory.get("127.0.0.1:8888");
        HttpClient httpClient = CloseableHttpClientFactory.getCloseableHttpClient(serverProviderAddress);
        //HttpClient httpClient = HttpClients.custom().build();

        HttpPost post = new HttpPost("http://127.0.0.1:8888");
        String body = "{\"iface\":\"demo.service.IFoo\",\"method\":\"echo\",\"args\":[{\"type\":\"java.lang.String\",\"value\":\"\\\"haha\\\"\"}]}";
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        try {
            HttpResponse rsp = httpClient.execute(post);
            int status = rsp.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new RuntimeException();
            }
            System.out.println(EntityUtils.toString(rsp.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
