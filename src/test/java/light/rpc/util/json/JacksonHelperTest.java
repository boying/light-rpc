package light.rpc.util.json;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class JacksonHelperTest {
    @Test
    public void StringTest() throws IOException {
        String a = "abc";
        String json = JacksonHelper.getMapper().writeValueAsString(a);
        System.out.println(json);
        String b = JacksonHelper.getMapper().readValue(json, String.class);
        System.out.println(b);
    }

    @Test
    public void String2Test() throws IOException {
        String result = JacksonHelper.getMapper().readValue("\"hello\"", String.class);
        System.out.println(result);
    }

    public static class T{
        public String a;
        public int b;
    }
    @Test
    public void String3Test() throws IOException {
        T t = new T();
        t.a = "haha";
        t.b = 123;

        String json = JacksonHelper.getMapper().writeValueAsString(t);
        System.out.println(json);


        T t1 = JacksonHelper.getMapper().readValue(json, T.class);
    }

    @Test
    public void intTest() throws IOException {
        int a = 123;
        String json = JacksonHelper.getMapper().writeValueAsString(a);
        System.out.println(json);
        int b = JacksonHelper.getMapper().readValue(json, int.class);
        System.out.println(b);
    }

    @Test
    public void IntegerTest() throws IOException {
        Integer a = 123;
        String json = JacksonHelper.getMapper().writeValueAsString(a);
        System.out.println(json);
        Integer b = JacksonHelper.getMapper().readValue(json, Integer.class);
        System.out.println(b);
    }

     @Test
    public void nullTest() throws IOException {
        Integer a = null;
        String json = JacksonHelper.getMapper().writeValueAsString(a);
        System.out.println(json);
        Integer b = JacksonHelper.getMapper().readValue(json, Integer.class);
        System.out.println(b);
    }

}
