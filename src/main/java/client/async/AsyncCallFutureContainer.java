package client.async;

import bean.Result;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by jiangzhiwen on 17/2/26.
 */
public class AsyncCallFutureContainer {
    private Map<Long, AsyncCallFuture<?>> map = new ConcurrentHashMap<>();

    public void addAsyncCallFuture(AsyncCallFuture<?> future) {
        map.put(future.getRequest().getAsyncReqId(), future);

    }

    public void discardAsyncCallFuture(AsyncCallFuture<?> future){
        map.remove(future.getRequest().getAsyncReqId());
    }

    public void discardAsyncCallFuture(Result result) {
        AsyncCallFuture<?> asyncCallFuture = map.get(result.getAsyncReqId());
        if(asyncCallFuture != null){
            asyncCallFuture.setResult(result);
            discardAsyncCallFuture(asyncCallFuture);
        }
    }

}
