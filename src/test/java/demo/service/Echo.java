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
        return s + " : " + " : " + in;
    }

    @Override
    public void f2() {
        System.out.println("f2 called");
    }

    @Override
    public Void f3() {
        System.out.println("f3 called");
        return null;
    }

    @Override
    public String haah() {
        return "not hahaha";
    }

    @Override
    public int add(int a, int... b) {
        int ret = a;
        if(b != null && b.length > 0){
            for(int i = 0; i < b.length; ++i){
                ret += b[i];
            }
        }
        return ret;
    }
}
