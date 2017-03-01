package demo.service;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public interface IEcho {
    String echo(String s);
    String f1(String s, int i, Integer in);
    void f2();
    Void f3();

    int add(int a, int... b);

    int sleep(int timeout);

    default String haah(){
        return "hahaha";
    }
}
