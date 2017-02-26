package client;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import server_provider.IServerProvider;
import util.HttpClientProvider;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
public class RequestJsonSender {
    public static String send(IServerProvider hostProvider, String body) {
        InetSocketAddress serverProviderAddress = hostProvider.get();
        HttpClient httpClient = HttpClientProvider.getHttpClient(serverProviderAddress);

        HttpPost post = new HttpPost(genHttpPostUrl(serverProviderAddress));
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        try {
            HttpResponse rsp = httpClient.execute(post);
            int status = rsp.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new RuntimeException();
            }
            return EntityUtils.toString(rsp.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String genHttpPostUrl(InetSocketAddress address) {
        return "http://" + address.getHostName() + ":" + address.getPort();
    }
}
