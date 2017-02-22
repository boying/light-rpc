package core;

import demo.service.Echo;
import demo.service.IEcho;
import server.ServiceBeanProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class RpcContestTest {
    public static void main(String[] args) throws Exception {
        /*
        List<Class<?>> types = new ArrayList<>();
        types.add(int.class);
        types.add(Integer.class);
        Object[] objects = types.toArray();
        Class[] classes = Arrays.copyOf(objects, objects.length, Class[].class);

        System.exit(0);


*/
        RpcContext context = new RpcContext("ConfigureZoo.json", new ServiceBeanProvider() {
            @Override
            public <T> T get(Class<T> clazz) {
                return (T) new Echo();
            }
        });
        context.init();

        while (true) {
            Scanner sc = new Scanner(System.in);
            String s = sc.nextLine();

            IEcho proxy = context.getProxy(IEcho.class);
            /*
            System.out.println(proxy.echo(s));

            System.out.println(proxy.f1("s", 1, 5));

            */
            proxy.f2();

            Void aVoid = proxy.f3();
            System.out.println(aVoid);
            System.out.println(proxy.haah());

            System.out.println(proxy.add(1));
            System.out.println(proxy.add(1, 2));
            System.out.println(proxy.add(8, null));

        }

    }
}
