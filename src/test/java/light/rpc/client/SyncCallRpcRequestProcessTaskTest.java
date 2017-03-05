package light.rpc.client;

import demo.service.IFoo;
import light.rpc.client.sync.SyncCallTask;
import light.rpc.result.Result;
import light.rpc.server_address_provider.ListedServerAddressProvider;
import light.rpc.util.InetSocketAddressFactory;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class SyncCallRpcRequestProcessTaskTest {
    @Test
    public void f() throws Exception {
        SyncCallTask syncCallTask = new SyncCallTask(new ListedServerAddressProvider(Arrays.asList(InetSocketAddressFactory.get("127.0.0.1:8888"))), IFoo.class.getMethod("echo", new Class[]{String.class}), new String[]{"hello"});
        Result result = syncCallTask.call();
        System.out.println(result);
    }
}
