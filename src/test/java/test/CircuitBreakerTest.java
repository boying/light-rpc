package test;

import light.rpc.core.ServiceBeanProvider;

import java.util.Map;

/**
 * Created by boying on 2018/11/12.
 */
public class CircuitBreakerTest {
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

    public static void main(String[] args) throws Exception {
        syncCallTest();
    }

    public static void syncCallTest() throws Exception {
    /*
        IFoo obj = new Foo();
        Map<Class, Object> map = new HashMap<>();
        map.put(IFoo.class, obj);
        RpcContextTest.MyServiceBeanProvider provider = new RpcContextTest.MyServiceBeanProvider(map);

        RpcContext context = new RpcContext("ConfigureNoZoo_gai.json", provider);

        context.start(true);

        IFoo foo = context.getProxy(IFoo.class);

        for(int i = 0; i < 100; ++i) {
            try {
                System.out.println("foo.throwException()");
                foo.throwException();
            } catch (Exception e) {
                System.out.println(e);
            }
            Thread.sleep(300);
        }

        System.out.println();

        System.out.println("foo.defaultFunc()");
        System.out.println(foo.defaultFunc("abc"));

        context.stop();
    */
    }

}
