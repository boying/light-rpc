package spring;

import demo.service.IFoo;
import org.springframework.stereotype.Service;

/**
 * Created by boying on 2018/11/12.
 */
@Service
public class Foo implements IFoo{
    @Override
    public String echo(String s) {
        return s;
    }

    @Override
    public String concat(String s, int i, Integer in) {
        return s + " : " + i + " : " + in;
    }

    @Override
    public String defaultFunc() {
        return "My Foo Func";
    }

    @Override
    public int add(int a, int... b) {
        int ret = a;
        if (b != null && b.length > 0) {
            for (int i = 0; i < b.length; ++i) {
                ret += b[i];
            }
        }
        return ret;
    }

    @Override
    public void voidFunc() {

    }

    @Override
    public void throwException() throws Exception {
        System.out.println("in throwException");
        throw new RuntimeException("Foo Exception");
    }

    @Override
    public void sleep(int timeout) {
        try {
            Thread.currentThread().sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
