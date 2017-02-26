package client.async;

import bean.Request;
import bean.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@RequiredArgsConstructor
@Data
public class AsyncCallFuture<T> implements Future<T> {
    private final AsyncCallFutureContainer asyncCallFutureContainer;
    private final Request request;
    private volatile Result result;
    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (result == null) {
            latch.await();
        }

        return parseResult();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (result == null) {
            if (latch.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }


        return parseResult();
    }

    private T parseResult() throws ExecutionException {
        // TODO
        return null;
    }

    public void setResult(Result result) {
        this.result = result;
        latch.countDown();
    }
}
