package light.rpc.client;

import light.rpc.client.sync.SyncCallTask;
import light.rpc.result.Result;
import demo.service.IEcho;
import org.junit.Test;
import light.rpc.server_provider.ListedServerProvider;
import light.rpc.util.InetSocketAddressFactory;

import java.util.Arrays;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class SyncCallTaskTest {
    @Test
    public void f() throws Exception {
        SyncCallTask syncCallTask = new SyncCallTask(new ListedServerProvider(Arrays.asList(InetSocketAddressFactory.get("127.0.0.1:8888"))), IEcho.class.getMethod("echo", new Class[]{String.class}), new String[]{"hello"});
        Result result = syncCallTask.call();
        System.out.println(result);
    }
}
