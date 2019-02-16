package test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by boying on 17/2/22.
 */
public class ProxyTest {
    public static interface A{
        void a();

        default void f1(){}
    }

    public static interface B extends A{
        void b();

        int haha();

        default void f2(){}
    }

    public static class C implements B{
        @Override
        public void b() {

        }

        @Override
        public int haha() {
            return 0;
        }

        @Override
        public void a() {

        }
    }

    public static void main(String[] args) throws InterruptedException {
        C c = new C();
        Method[] methods1 = c.getClass().getMethods();
        for (Method method : methods1) {
            System.out.println(method);
        }
        System.exit(0);
        System.out.println(A.class.toString());

        System.out.println("xx");

        Method[] methods = B.class.getMethods();
        for (Method method : methods) {
            System.out.println(method);
        }

        System.out.println("==============");

        Method[] declaredMethods = B.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            System.out.println(declaredMethod);
        }

        System.out.println("===============");
        for (Method method : Object.class.getMethods()) {
            System.out.println(method);
        }


        System.out.println("===============");

        B b = (B)Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class[]{B.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //System.out.println(b == proxy);
                System.out.println("hahaha: " + System.identityHashCode(proxy));
                if(method.getDeclaringClass() == Object.class){
                    System.out.println("enenenenenen");
                    return method.invoke(proxy, args);
                }
                System.out.println(proxy instanceof B);
                return null;
            }
        });

        //b.b();
        int i = System.identityHashCode(b);
        System.out.println(i);
        b.toString();

        //b.haha();
        /*
        b.getClass();
        //b.hashCode();

        synchronized (b){
            b.wait();
            System.out.println("haha");
        }

        Void func = null;
        */

    }
}
