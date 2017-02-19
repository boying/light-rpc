package server;

import conf.ServerConf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
@RequiredArgsConstructor
public class JsonServer implements Server {
    @NonNull
    private final ServerConf serverConf;

    @Override
    public void init() {
    }

    @Override
    public void start() {

        ResponseSerializer responseSerializer = new ResponseSerializer();
        HttpServerCodec httpServerCodec = new HttpServerCodec();
        HttpServerAggregator httpServerAggregator = new HttpServerAggregator();
        HttpServerMethodInvoker httpServerMethodInvoker = new HttpServerMethodInvoker(serverConf);

        ServerBootstrap serverBootstrap = ServerBootstrapFactory.newServerBootstrap(1, 100, true);
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(httpServerCodec);
                ch.pipeline().addLast(httpServerAggregator);
                ch.pipeline().addLast(httpServerMethodInvoker);
                ch.pipeline().addLast(responseSerializer);
            }
        });

        try {
            serverBootstrap.bind(serverConf.getPort()).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {

    }
}
