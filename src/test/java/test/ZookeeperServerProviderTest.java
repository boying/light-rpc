package test;

import light.rpc.server_address_provider.ZooKeeperServerAddressProvider;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by boying on 17/2/17.
 */
public class ZookeeperServerProviderTest {
    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2181);
        System.out.println(inetSocketAddress.toString());
        System.out.println(inetSocketAddress.getHostName());
        System.out.println(inetSocketAddress.getHostString());
        System.out.println(inetSocketAddress.getAddress().getHostName());
        System.out.println(inetSocketAddress.getAddress().getHostAddress());
        System.out.println("----------------");

        ZooKeeperServerAddressProvider provider = new ZooKeeperServerAddressProvider(new InetSocketAddress("127.0.0.1", 2181) , "test");
        provider.init();

        while (true){
            InetSocketAddress address = provider.get();
            System.out.println(address);
            Thread.sleep(1000);
        }

    }
}
