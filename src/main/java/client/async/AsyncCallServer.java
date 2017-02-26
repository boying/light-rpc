package client.async;

import bean.Result;
import lombok.RequiredArgsConstructor;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@RequiredArgsConstructor
public class AsyncCallServer {
    private final int port;
    private final AsyncCallFutureContainer asyncCallFutureContainer;

    public void init() {

    }

    public void start() {

    }

    private void processResult(Result result) {
        asyncCallFutureContainer.discardAsyncCallFuture(result);
    }
}
