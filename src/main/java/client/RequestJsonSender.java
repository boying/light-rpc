package client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server_provider.IServerProvider;
import util.CloseableHttpClientFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
public class RequestJsonSender {
    private static final Logger logger = LoggerFactory.getLogger(RequestJsonSender.class);

    public static String send(IServerProvider hostProvider, String body) throws IOException {
        InetSocketAddress serverProviderAddress = hostProvider.get();
        CloseableHttpClient httpClient = CloseableHttpClientFactory.getCloseableHttpClient(serverProviderAddress);

        HttpPost post = new HttpPost(genHttpPostUrl(serverProviderAddress));
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        logger.debug("post req is {}", post);
        CloseableHttpResponse rsp = httpClient.execute(post);
        logger.debug("rsp is {}", rsp);
        String ret = EntityUtils.toString(rsp.getEntity());
        rsp.close();
        return ret;
    }

    private static String genHttpPostUrl(InetSocketAddress address) {
        return "http://" + address.getHostName() + ":" + address.getPort();
    }
}
