package client.async;

import bean.Request;
import bean.Result;
import exception.ClientException;
import exception.ServerException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@RequiredArgsConstructor
@Data
public class AsyncCallFuture<T> implements Future<T> {
    private final Method method;
    private final Request request;
    private volatile Result result;
    private volatile ClientException clientException;
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
            if (!latch.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }

        return parseResult();
    }

    private T parseResult() throws ExecutionException {
        if (this.clientException != null) {
            throw this.clientException;
        }

        if (result.isInvokedSuccess()) {
            if (result.getThrowable() != null) {
                throw new ExecutionException(result.getThrowable());
            } else {
                return (T) result.getResult();
            }
        }

        throw new ExecutionException(new ServerException(result.getErrorMsg()));
    }

    public void setResult(Result result) {
        this.result = result;
        latch.countDown();
    }

    public void setClientException(ClientException clientException) {
        this.clientException = clientException;
        latch.countDown();
    }
}
