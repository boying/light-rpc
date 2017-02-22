import demo.service.IEcho;

import java.lang.reflect.Method;
import java.util.List;

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


    public static void main(String[] args) throws ClassNotFoundException {
        //System.out.println(int.class.getName());
        //System.out.println(int.class.getTypeName());
        //Class<?> anInt = Class.forName("I");

        /*
        System.out.println(int.class == Integer.class);
        System.out.println(int.class == Integer.TYPE);
        */

        System.out.println(int[].class.getName());
        System.out.println(Class.forName(int[].class.getName()));

    }
}
