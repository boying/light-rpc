package light.rpc.client.sync;

import light.rpc.client.Json2ResultDeserializer;
import light.rpc.client.Request2JsonSerializer;
import light.rpc.client.RequestFactory;
import light.rpc.client.RequestJsonSender;
import light.rpc.protocol.Request;
import light.rpc.result.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import light.rpc.server_provider.IServerProvider;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * rpc同步调用任务
 */
@RequiredArgsConstructor
public class SyncCallTask implements Callable<Result> {
    private static Logger logger = LoggerFactory.getLogger(SyncCallTask.class);

    /**
     * rpc服务方提供者
     */
    @NonNull
    private final IServerProvider hostProvider;

    /**
     * 调用方法
     */
    @NonNull
    private final Method method;

    /**
     * 方法参数
     */
    @NonNull
    private final Object[] args;

    @Override
    public Result call() throws Exception {
        Request request = RequestFactory.newRequest(method, args, false, 0);
        String body = Request2JsonSerializer.serialize(request);
        logger.debug("request is {}", body);
        String rsp = RequestJsonSender.send(hostProvider, body);
        logger.debug("response is {}", rsp);
        return Json2ResultDeserializer.deserialize(rsp, method.getReturnType());
    }

}
