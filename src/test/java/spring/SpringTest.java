package spring;

import demo.service.IFoo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by boying on 2018/11/12.
 */
public class SpringTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        String[] beanNamesForType = context.getBeanNamesForType(IFoo.class);
        for (String s : beanNamesForType) {
            System.out.println(s);
        }

        IFoo foo = (IFoo)context.getBean("lightRpcClient_1_IFoo");
        test(foo);
    }

    public static void test(IFoo foo){
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
        System.out.println();

    }
}
