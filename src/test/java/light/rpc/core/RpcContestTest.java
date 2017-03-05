package light.rpc.core;

import demo.service.Echo;
import demo.service.IEcho;

import java.util.Scanner;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class RpcContestTest {
    public static void main(String[] args) throws Exception {
        RpcContext context = new RpcContext("Configure.json", new ServiceBeanProvider() {
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

            proxy.toString();
            proxy.hashCode();
            proxy.getClass();
            /*
            System.out.println(proxy.echo(s));

            System.out.println(proxy.f1("s", 1, 5));

            */
            try {
                proxy.f2();
            }catch (Exception e){
                System.out.println(e);
            }

            Void aVoid = proxy.f3();
            System.out.println(aVoid);
            System.out.println(proxy.haah());

            System.out.println(proxy.add(1));
            System.out.println(proxy.add(1, 2));
            System.out.println(proxy.add(8, null));

        }

    }
}
