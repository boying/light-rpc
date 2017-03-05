package demo.service;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class Foo implements IFoo {
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
