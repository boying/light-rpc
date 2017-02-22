package server;

import conf.ServerConf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
@RequiredArgsConstructor
public class HttpServer implements Server {
    @NonNull
    private final ServerConf serverConf;
    private ExecutorService executorService;
    private static final int WAITING_QUEUE_SIZE = 1000;

    @Override
    public void init() {
    }

    @Override
    public void start() {
        executorService = new ThreadPoolExecutor(0, serverConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WAITING_QUEUE_SIZE));
        ServerBootstrap serverBootstrap = ServerBootstrapFactory.newServerBootstrap(1, 100, true);
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                //Todo this use will call error, puzzled
                // pipeline.addLast(new ResponseSerializer());

                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpServerAggregator());
                pipeline.addLast(new HttpServerMethodInvoker(serverConf, executorService));
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
