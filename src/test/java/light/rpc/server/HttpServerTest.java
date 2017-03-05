package light.rpc.server;

import demo.service.Foo;
import light.rpc.conf.Conf;
import light.rpc.conf.ConfParser;
import light.rpc.core.ServiceBeanProvider;

/**
 * Created by jiangzhiwen on 17/2/21.
 */
public class HttpServerTest {
    public static void main(String[] args) throws Exception {
        Conf conf = ConfParser.parseByPath("Configure.json");
        conf.getServerConf().setServiceBeanProvider(new ServiceBeanProvider(){
            @Override
            public <T> T get(Class<T> clazz) {
                return (T) new Foo();
            }
        });
        Server server = new HttpServer(conf.getServerConf());
        server.start();
    }
}
