package demo.service;

/**
 * Created by boying on 17/2/21.
 */
public interface IFoo {
    Response<FuncData> func(String str, Long val);

    String echo(String s);

    int add(int a, int... b);

    String concat(String s, int i, Integer in);

    void sleep(int timeout);
}
