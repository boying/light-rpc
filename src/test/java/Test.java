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


    public static void main(String[] args) {
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

}
