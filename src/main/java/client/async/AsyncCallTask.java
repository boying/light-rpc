package client.async;

import bean.AsyncResponse;
import bean.Request;
import client.RequestFactory;
import client.RequestJsonSender;
import client.Request2JsonSerializer;
import exception.ClientException;
import exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server_provider.IServerProvider;
import util.json.JacksonHelper;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@RequiredArgsConstructor
public class AsyncCallTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCallTask.class);
    private final Class clazz;
    private final Method method;
    private final Object[] args;
    private final IServerProvider serverProvider;
    private final AsyncCallFutureContainer asyncCallFutureContainer;
    private final int port;

    public Future<T> getFuture() {
        Request request = RequestFactory.newRequest(clazz, method, args, true, port);
        AsyncCallFuture<T> future = new AsyncCallFuture<>(method, request);
        String jsonReq = Request2JsonSerializer.serialize(request);
        asyncCallFutureContainer.addAsyncCallFuture(future);
        AsyncResponse asyncResponse;
        try {
            logger.debug("send async call req {}", jsonReq);
            String jsonRsp = RequestJsonSender.send(serverProvider, jsonReq);
            asyncResponse = JacksonHelper.getMapper().readValue(jsonRsp, AsyncResponse.class);
            logger.debug("send async call success");
        } catch (Exception e) {
            asyncCallFutureContainer.discardAsyncCallFuture(future);
            logger.debug("send async call failed, ", e);
            throw new ClientException(e);
        }

        if (!asyncResponse.isAcceptedSuccess()) {
            asyncCallFutureContainer.discardAsyncCallFuture(future);
            logger.debug("async call req ack failed");
            throw new ServerException(asyncResponse.getErrorMsg());
        }
        logger.debug("async call req ack success");

        return future;
    }

}
