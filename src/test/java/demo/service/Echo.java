package demo.service;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class Echo implements IEcho{
    @Override
    public String echo(String s) {
        return s;
    }

    @Override
    public String f1(String s, int i, Integer in) {
        return null;
    }

    @Override
    public void f2() {

    }

    @Override
    public Void f3() {
        return null;
    }
}
