package demo.service;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public interface IFoo {
    String echo(String s);

    int add(int a, int... b);

    String concat(String s, int i, Integer in);

    void sleep(int timeout);

    void voidFunc();

    void throwException() throws Exception;


    default String defaultFunc() {
        return "IFoo default func called";
    }
}
