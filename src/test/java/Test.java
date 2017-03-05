import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
public class Test {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Map map = new HashMap<>();
        map.remove(new Object());
        System.exit(0);
        Future<Void> submit = Executors.newCachedThreadPool().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        });

        Void o = submit.get();

    }


}
