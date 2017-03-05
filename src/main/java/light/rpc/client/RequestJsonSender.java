package light.rpc.client;

import light.rpc.server_address_provider.IServerAddressProvider;
import light.rpc.util.CloseableHttpClientFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Request Json串请求发送者
 */
public class RequestJsonSender {
    private static final Logger logger = LoggerFactory.getLogger(RequestJsonSender.class);

    /**
     * 发送调用http请求
     *
     * @param addressProvider 服务方提供者
     * @param body            请求串
     * @return
     * @throws IOException
     */
    public static String send(IServerAddressProvider addressProvider, String body) throws IOException {
        InetSocketAddress serverProviderAddress = addressProvider.get();
        CloseableHttpClient httpClient = CloseableHttpClientFactory.getCloseableHttpClient(serverProviderAddress);

        HttpPost post = new HttpPost("http://" + serverProviderAddress.getHostName() + ":" + serverProviderAddress.getPort());
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        logger.debug("post req is {}", post);
        CloseableHttpResponse rsp = httpClient.execute(post);
        logger.debug("rsp is {}", rsp);
        String ret = EntityUtils.toString(rsp.getEntity());
        rsp.close();
        return ret;
    }
}
