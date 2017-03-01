import core.RpcContext;
import core.ServiceBeanProvider;
import demo.service.Echo;
import demo.service.IEcho;

import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangzhiwen on 17/3/1.
 */
public class AsyncCallTest {
    public static void main(String[] args) throws Exception {
        RpcContext context = new RpcContext("Configure.json", new ServiceBeanProvider() {
            @Override
            public <T> T get(Class<T> clazz) {
                return (T) new Echo();
            }
        });
        context.init();


        IEcho proxy = context.getProxy(IEcho.class);

        Future<Integer> future = context.asyncCall(IEcho.class, "sleep", new Class<?>[]{int.class}, new Object[]{1000}, int.class);
        System.out.println("XXXXXXXXXXXXXXXXX");
        Integer integer = future.get(5, TimeUnit.SECONDS);
        System.out.println("XXXXXXXXXXXXXXXXX");
        System.out.println(integer);

    }
}