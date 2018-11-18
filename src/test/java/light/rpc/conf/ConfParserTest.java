package light.rpc.conf;

/**
 * Created by jiangzhiwen on 17/3/5.
 */
public class ConfParserTest {
    public static void main(String[] args) throws Exception {
        Config conf = ConfParser.parseByPath("Configure.json");
    }
}
