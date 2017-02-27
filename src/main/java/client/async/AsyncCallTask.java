package client.async;

import bean.AsyncResponse;
import bean.Request;
import client.RequestFactory;
import client.RequestJsonSender;
import client.Request2JsonSerializer;
import exception.ClientException;
import exception.ServerException;
import lombok.RequiredArgsConstructor;
import server_provider.IServerProvider;
import util.json.JacksonHelper;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@RequiredArgsConstructor
public class AsyncCallTask<T> {
    private final Method method;
    private final Object[] args;
    private final IServerProvider serverProvider;
    private final AsyncCallFutureContainer asyncCallFutureContainer;
    private final int port;

    public Future<T> getFuture() {
        Request request = RequestFactory.newRequest(method, args, true, port);
        AsyncCallFuture<T> future = new AsyncCallFuture<>(method, request);
        String jsonReq = Request2JsonSerializer.serialize(request);
        asyncCallFutureContainer.addAsyncCallFuture(future);
        AsyncResponse asyncResponse;
        try {
            String jsonRsp = RequestJsonSender.send(serverProvider, jsonReq);
            asyncResponse = JacksonHelper.getMapper().readValue(jsonRsp, AsyncResponse.class);
        } catch (Exception e) {
            asyncCallFutureContainer.discardAsyncCallFuture(future);
            throw new ClientException(e);
        }

        if (!asyncResponse.isAcceptedSuccess()) {
            asyncCallFutureContainer.discardAsyncCallFuture(future);
            throw new ServerException(asyncResponse.getErrorMsg());
        }

        return future;
    }

}