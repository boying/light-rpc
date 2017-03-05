package light.rpc.client.async;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步调用请求id生成器
 */
public class RequestIdGenerator {
    private static AtomicLong id = new AtomicLong(0);

    /**
     * 生成一个异步调用请求id
     * @return
     */
    public static long genId(){
        return Math.abs(id.incrementAndGet());
    }
}
