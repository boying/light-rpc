package test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import light.rpc.util.ServerBootstrapFactory;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
public class NettyTest {
    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = ServerBootstrapFactory.newServerBootstrap(1, 10, true);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {

            }
        });

        serverBootstrap.bind(1232).sync();
        System.out.println("hahaha");
    }

}
