package core;

import demo.service.IEcho;

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
        RpcContext context =  new RpcContext("Configure.json", null);
        context.init();

        Scanner sc = new Scanner(System.in);
        sc.nextLine();

        IEcho proxy = context.getProxy(IEcho.class);
        System.out.println(proxy.echo("haha"));
    }
}
