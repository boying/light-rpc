package client;

import bean.Result;
import demo.service.IEcho;
import org.junit.Test;
import server_provider.ListedServerProvider;
import util.InetSocketAddressFactory;

import java.util.Arrays;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class TaskTest {
    @Test
    public void f() throws Exception {
        Task task = new Task(new ListedServerProvider(Arrays.asList(InetSocketAddressFactory.get("127.0.0.1:8888"))), IEcho.class.getMethod("echo", new Class[]{String.class}), new String[]{"hello"});
        Result result = task.call();
        System.out.println(result);
    }
}
