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
        //syncCallTest();
        asyncCallTest();
    }

    public static void syncCallTest() throws Exception {
        IFoo echo = new Foo();
        Map<Class, Object> map = new HashMap<>();
        map.put(IFoo.class, echo);
        MyServiceBeanProvider provider = new MyServiceBeanProvider(map);

        // 不使用ZooKeeper注册中心
        RpcContext context = new RpcContext("ConfigureNoZoo.json", provider);

        // 使用ZooKeeper注册中心
        //RpcContext context = new RpcContext("ConfigureZoo.json", provider);

        context.start();

        IFoo foo = context.getProxy(IFoo.class);

        Scanner sc = new Scanner(System.in);
        sc.nextLine();

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

    public static void asyncCallTest() throws Exception {
        IFoo echo = new Foo();
        Map<Class, Object> map = new HashMap<>();
        map.put(IFoo.class, echo);
        MyServiceBeanProvider provider = new MyServiceBeanProvider(map);

        // 不使用ZooKeeper注册中心
        RpcContext context = new RpcContext("ConfigureNoZoo.json", provider);

        // 使用ZooKeeper注册中心
        //RpcContext context = new RpcContext("ConfigureZoo.json", provider);

        context.start();

        Scanner sc = new Scanner(System.in);
        sc.nextLine();

        System.out.println("foo.echo(\"hello world!\")");
        Future<String> echo1 = context.asyncCall(IFoo.class, "echo", new Class[]{String.class}, new Object[]{"hello world!"}, String.class);
        String s = echo1.get();
        System.out.println(s);
        System.out.println();

        System.out.println("foo.add(1, 2, 3)");
        Future<Integer> future = context.asyncCall(IFoo.class, "add", new Class[]{int.class, int[].class}, new Object[]{1, new Object[]{2, 3}}, int.class);
        System.out.println(future.get());
        System.out.println();

        System.out.println("foo.concat(\"s\", 1, null);");
        Future<String> concat = context.asyncCall(IFoo.class, "concat", new Class[]{String.class, int.class, Integer.class}, new Object[]{"s", 1, null}, String.class);
        String s1 = concat.get();
        System.out.println(s1);
        System.out.println();

        System.out.println("foo.sleep(5000)");
        Future<Void> sleep = null;
        try {
            sleep = context.asyncCall(IFoo.class, "sleep", new Class[]{int.class}, new Object[]{5000}, Void.class);
            Void aVoid = sleep.get(1, TimeUnit.SECONDS);
            System.out.println(aVoid);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (sleep != null) {
                sleep.cancel(true);
            }
        }
        System.out.println();

        System.out.println("foo.sleep(10)");
        sleep = context.asyncCall(IFoo.class, "sleep", new Class[]{int.class}, new Object[]{10}, Void.class);
        Void aVoid = sleep.get();
        System.out.println(aVoid);
        System.out.println();

        System.out.println("foo.voidFunc()");
        Future<Void> voidFunc = context.asyncCall(IFoo.class, "voidFunc");
        System.out.println(voidFunc.get());
        System.out.println();

        System.out.println("foo.throwException()");
        Future<Void> throwException = context.asyncCall(IFoo.class, "throwException");
        try {
            throwException.get();
        } catch (ExecutionException e) {
            System.out.println(e.getCause());
        }
        System.out.println();

        System.out.println("foo.defaultFunc()");
        Future<String> defaultFunc = context.asyncCall(IFoo.class, "defaultFunc", String.class);
        System.out.println(defaultFunc.get());

        context.close();
    }
}
