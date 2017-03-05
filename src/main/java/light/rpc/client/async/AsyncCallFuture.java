package light.rpc.client.async;

import light.rpc.protocol.Request;
import light.rpc.result.Result;
import light.rpc.exception.ClientException;
import light.rpc.exception.ServerException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * 异步调用Future
 * 可用来获取异步调用结果
 */
@RequiredArgsConstructor
@Data
public class AsyncCallFuture<T> implements Future<T> {
    /**
     * 异步调用的方法对象
     */
    private final Method method;

    /**
     * 异步调用对应的请求对象
     */
    private final Request request;

    private volatile Result result;
    private volatile ClientException clientException;
    private CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean canceled = false;

    /**
     * 设置异步调用的结果
     *
     * @param result
     */
    public void setResult(Result result) {
        this.result = result;
        latch.countDown();
    }

    /**
     * 设置异步调用的
     *
     * @param clientException
     */
    public void setClientException(ClientException clientException) {
        this.clientException = clientException;
        latch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        canceled = true;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (latch.getCount() != 0) {
            latch.await();
        }

        return parseResult();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (latch.getCount() != 0) {
            if (!latch.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }

        return parseResult();
    }

    private T parseResult() throws ExecutionException {
        if (this.clientException != null) {
            throw new ExecutionException(clientException);
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
}
