package light.rpc.client.async;

import light.rpc.exception.ClientException;
import light.rpc.result.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 异步调用Future容器
 * 内部维护一个map,key是异步调用请求id,value是异步调用Future对象
 */
public class AsyncCallFutureContainer {
    private Map<Long, AsyncCallFuture<?>> map = new ConcurrentHashMap<>();

    /**
     * 添加一个异步调用Future
     *
     * @param future
     */
    public void addAsyncCallFuture(AsyncCallFuture<?> future) {
        map.put(future.getRequest().getAsyncReqId(), future);
    }

    /**
     * 根据请求id获取异步调用Future
     *
     * @param reqId
     * @return
     */
    public AsyncCallFuture<?> getByReqId(long reqId) {
        return map.get(reqId);
    }

    /**
     * @param future future对象
     */
    public void discardAsyncCallFuture(AsyncCallFuture<?> future) {
        map.remove(future.getRequest().getAsyncReqId());
    }

    public void discardAsyncCallFuture(Result result) {
        AsyncCallFuture<?> asyncCallFuture = map.get(result.getAsyncReqId());
        if (asyncCallFuture != null) {
            asyncCallFuture.setResult(result);
            discardAsyncCallFuture(asyncCallFuture);
        }
    }

    public void discardAsyncCallFuture(AsyncCallFuture<?> future, ClientException clientException) {
        future.setClientException(clientException);
        map.remove(future.getRequest().getAsyncReqId());
    }

}
