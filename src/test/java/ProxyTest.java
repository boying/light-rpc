import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by jiangzhiwen on 17/2/22.
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

    public static void main(String[] args) throws InterruptedException {
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
                System.out.println(proxy instanceof B);
                return null;
            }
        });

        //b.b();
        b.a();
        b.toString();
        //b.haha();
        b.getClass();
        //b.hashCode();

        synchronized (b){
            b.wait();
            System.out.println("haha");
        }

    }
}
