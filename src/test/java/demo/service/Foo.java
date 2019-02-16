package demo.service;

/**
 * Created by boying on 17/2/21.
 */
public class Foo implements IFoo {
    @Override
    public Response<FuncData> func(String str, Long val) {
        FuncData funcData = new FuncData(str, val);
        Response<FuncData> rsp = new Response<>();
        rsp.setData(funcData);
        return rsp;
    }

    @Override
    public String echo(String s) {
        return s;
    }

    @Override
    public String concat(String s, int i, Integer in) {
        return s + " : " + i + " : " + in;
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
    public void sleep(int timeout) {
        try {
            Thread.currentThread().sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
