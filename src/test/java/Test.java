import demo.service.IEcho;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import util.CloseableHttpClientFactory;
import util.InetSocketAddressFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
public class Test {
    public void f1(){}

    public void f2(int a){
        System.out.println("a called");
    }

    public void f2(int... a){
        System.out.println("a... called");
    }

    public void x2(int a, int... b){}

    public void f3(Integer a){}

    public void f4(List list){}


    public static void main1(String[] args) {
        Method[] methods = Test.class.getMethods();
        for (Method method : methods) {
            System.out.print(method.getName() + "(");
            //System.out.println(method.getParameterTypes());
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                //parameterType.getDeclaringClass();
                System.out.print(parameterType.getTypeName() + ", ");
            }
            System.out.println(")");

        }
    }


    public static void main2(String[] args) {
        Method[] methods = IEcho.class.getMethods();
        for (Method method : methods) {

            System.out.println(method);
        }
    }

    private static String genHttpPostUrl(InetSocketAddress address) {
        return "http://" + address.getHostName() + ":" + address.getPort();
    }

    public static void main(String[] args) {
        HttpClient httpClient = CloseableHttpClientFactory.getCloseableHttpClient(
                InetSocketAddressFactory.get("127.0.0.1", 9999)
        );

        String body = "{\"async\": true, \"asyncReqId\": 123, \"invokedSuccess\": true, \"result\": \"null\", \"throwable\": \"null\", \"errorMsg\": \"msg\"}";
        HttpPost post = new HttpPost("http://127.0.0.1:9999");
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        try {
            HttpResponse rsp = httpClient.execute(post);
            int status = rsp.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new RuntimeException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main3(String[] args) throws ClassNotFoundException {
        //System.out.println(int.class.getName());
        //System.out.println(int.class.getTypeName());
        //Class<?> anInt = Class.forName("I");

        /*
        System.out.println(int.class == Integer.class);
        System.out.println(int.class == Integer.TYPE);
        */

        //System.out.println(Runtime.getRuntime().availableProcessors());
        /*
        System.out.println(int[].class.getName());
        System.out.println(Class.forName(int[].class.getName()));
        */

        AtomicInteger i = new AtomicInteger(Integer.MAX_VALUE);
        System.out.println(i);
        System.out.println(i.incrementAndGet());
        System.out.println(-4 % 3);
        H.h();

    }

    public static abstract class H{
        public abstract void f();
        public static void h(){
            System.out.println("haha");
        }
    }
}
