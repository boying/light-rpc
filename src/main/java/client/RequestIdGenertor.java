package client;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by jiangzhiwen on 17/2/27.
 */
public class RequestIdGenertor {
    private static AtomicLong id = new AtomicLong(0);

    public static long genId(){
        return Math.abs(id.incrementAndGet());
    }
}
