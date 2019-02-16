package light.rpc.util.json;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by boying on 17/2/21.
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

    public static class T {
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
        Assert.assertEquals(a.toString(), json);
        Integer b = JacksonHelper.getMapper().readValue(a.toString(), Integer.class);
        Assert.assertEquals(a, b);
    }

    @Test
    public void nullTest() throws IOException {
        Assert.assertEquals("null", JacksonHelper.getMapper().writeValueAsString(null));
        Assert.assertNull(JacksonHelper.getMapper().readValue("null", Integer.class));

        Assert.assertEquals("null", new Gson().toJson(null));
        Assert.assertNull(new Gson().fromJson("null", Integer.class));
    }
}
