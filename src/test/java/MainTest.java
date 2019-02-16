import com.google.gson.Gson;
import demo.service.Foo;
import demo.service.FuncData;
import demo.service.IFoo;
import demo.service.Response;
import light.rpc.core.RpcContext;
import light.rpc.core.ServiceBeanProvider;
import light.rpc.exception.ClientException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by boying on 17/2/12.
 */
public class MainTest {
    private IFoo foo;
    private RpcContext clientContext;
    private RpcContext serverContext;

    @Before
    public void init() throws Exception {
        startClient();
        startServer();
    }

    private void startClient() throws Exception {
        clientContext = new RpcContext("light_rpc_config_client.yaml", null);
        clientContext.start(false);
        foo = clientContext.getProxy(IFoo.class);
    }

    public static class MyServiceBeanProvider implements ServiceBeanProvider {
        private Map<Class, Object> map;

        public MyServiceBeanProvider(Map<Class, Object> map) {
            this.map = map;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Class<T> clazz) {
            return (T) map.get(clazz);
        }
    }

    private void startServer() throws Exception {
        IFoo obj = new Foo();
        Map<Class, Object> map = new HashMap<>();
        map.put(IFoo.class, obj);
        ServiceBeanProvider provider = new MyServiceBeanProvider(map);

        serverContext = new RpcContext("light_rpc_config_server.yaml", provider);

        serverContext.start(false);
    }

    @After
    public void exit() {
        clientContext.stop();
        serverContext.stop();
    }

    @Test
    public void funcTest() {
        String str = "abc";
        Long val = 123L;
        Response<FuncData> rsp = foo.func(str, val);

        Response<FuncData> expectedRsp = new Response<>();
        expectedRsp.setData(new FuncData(str, val));

        Assert.assertEquals(new Gson().toJson(expectedRsp), new Gson().toJson(rsp));

        rsp = foo.func(null, null);
        expectedRsp = new Response<>();
        expectedRsp.setData(new FuncData());
        Assert.assertEquals(new Gson().toJson(expectedRsp), new Gson().toJson(rsp));
    }

    @Test
    public void echoTest() {
        String expected = "abc";
        String actual = foo.echo(expected);
        Assert.assertEquals(expected, actual);

        actual = foo.echo(null);
        Assert.assertNull(actual);
    }

    @Test
    public void addTest() {
        int a = 1, b = 2, c = 3;
        Assert.assertEquals(a, foo.add(a));
        Assert.assertEquals(a + b, foo.add(a, b));
        Assert.assertEquals(a + b + c, foo.add(a, b, c));
    }

    @Test
    public void concatTest() {
        String s = "a";
        int i = 1;
        Integer in = null;
        Assert.assertEquals(s + " : " + i + " : " + in, foo.concat(s, i, in));

    }

    @Test
    public void sleepTest() {
        foo.sleep(10);
        Assert.assertTrue(true);
    }

    @Test(expected = ClientException.class)
    public void sleepOverTest() {
        foo.sleep(1000);
    }


}
