package light.rpc.client.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import light.rpc.protocol.AsyncCallAckResponse;
import light.rpc.protocol.Request;
import light.rpc.client.Request2JsonSerializer;
import light.rpc.client.RequestFactory;
import light.rpc.client.RequestJsonSender;
import light.rpc.exception.ClientException;
import light.rpc.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import light.rpc.server_provider.IServerProvider;
import light.rpc.util.json.JacksonHelper;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * 异步调用任务
 * 主要用于获取异步调用Future对象,可用此对象获取异步调用结果
 */
@RequiredArgsConstructor
public class AsyncCallTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCallTask.class);

    /**
     * 异步调用类
     */
    private final Class clazz;

    /**
     * 异步调用方法
     */
    private final Method method;

    /**
     * 方法参数
     */
    private final Object[] args;

    /**
     * rpc服务方提供者
     */
    private final IServerProvider serverProvider;

    /**
     * 异步调用Future容器
     */
    private final AsyncCallFutureContainer asyncCallFutureContainer;

    /**
     * 异步调用结果接受服务器的端口
     */
    private final int port;

    public Future<T> getFuture() throws JsonProcessingException {
        Request request = RequestFactory.newRequest(clazz, method, args, true, port);
        AsyncCallFuture<T> future = new AsyncCallFuture<>(method, request);
        String jsonReq = Request2JsonSerializer.serialize(request);
        asyncCallFutureContainer.addAsyncCallFuture(future);
        AsyncCallAckResponse asyncCallAckResponse;
        try {
            logger.debug("send async call req {}", jsonReq);
            String jsonRsp = RequestJsonSender.send(serverProvider, jsonReq);
            asyncCallAckResponse = JacksonHelper.getMapper().readValue(jsonRsp, AsyncCallAckResponse.class);
            logger.debug("send async call success");
        } catch (Exception e) {
            asyncCallFutureContainer.discardAsyncCallFuture(future);
            logger.debug("send async call failed, ", e);
            throw new ClientException(e);
        }

        if (!asyncCallAckResponse.isAcceptedSuccess()) {
            asyncCallFutureContainer.discardAsyncCallFuture(future);
            logger.debug("async call req ack failed");
            throw new ServerException(asyncCallAckResponse.getErrorMsg());
        }
        logger.debug("async call req ack success");

        return future;
    }

}
