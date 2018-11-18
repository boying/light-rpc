package light.rpc.core;

import demo.service.Foo;
import demo.service.IFoo;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class RpcContextTest {
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
        IFoo obj = new Foo();
        Map<Class, Object> map = new HashMap<>();
        map.put(IFoo.class, obj);
        MyServiceBeanProvider provider = new MyServiceBeanProvider(map);

        // 不使用ZooKeeper注册中心
        RpcContext context = new RpcContext("ConfigureNoZoo_gai.json", provider);

        // 使用ZooKeeper注册中心
        //RpcContext context = new RpcContext("ConfigureZoo.json", provider);

        context.start(true);

        IFoo foo = context.getProxy(IFoo.class);

        System.out.println("foo.toString()");
        String s2 = foo.toString();
        System.out.println(s2);
        System.out.println();


        System.out.println("foo.hashCode()");
        int i = foo.hashCode();
        System.out.println(i);
        System.out.println();

        System.out.println("foo.getClass()");
        Class<? extends IFoo> aClass = foo.getClass();
        System.out.println(aClass);
        System.out.println();

        System.out.println("foo.echo(\"hello world!\")");
        String s = foo.echo("hello world!");
        System.out.println(s);
        System.out.println();

        System.out.println("foo.add(1, 2, 3)");
        int add = foo.add(1, 2, 3);
        System.out.println(add);
        System.out.println();

        System.out.println("foo.concat(\"s\", 1, null);");
        String s1 = foo.concat("s", 1, null);
        System.out.println(s1);
        System.out.println();

        try {
            System.out.println("foo.sleep(5000);");
            foo.sleep(5000);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println();


        System.out.println("foo.sleep(10);");
        foo.sleep(10);
        System.out.println();

        System.out.println("foo.voidFunc()");
        foo.voidFunc();
        System.out.println();

        try {
            System.out.println("foo.throwException()");
            foo.throwException();
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println();

        System.out.println("foo.defaultFunc()");
        System.out.println(foo.defaultFunc());

        context.close();
    }

}
