package server;

import conf.ServerConf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import util.ServerBootstrapFactory;

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
    private ServerBootstrap serverBootstrap;

    @Override
    public void init() {
    }

    @Override
    public void start() {
        executorService = new ThreadPoolExecutor(0, serverConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WAITING_QUEUE_SIZE));
        serverBootstrap = ServerBootstrapFactory.newServerBootstrap(true);

        // Tcp参数设置
        // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

        //socket心跳机制
        /*
        SO_KEEPALIVE 保持连接检测对方主机是否崩溃，避免（服务器）永远阻塞于TCP连接的输入。
设置该选项后，如果2小时内在此套接口的任一方向都没有数据交换，TCP就自动给对方 发一个保持存活探测分节(keepalive probe)。这是一个对方必须响应的TCP分节.它会导致以下三种情况：
1、对方接收一切正常：以期望的ACK响应，2小时后，TCP将发出另一个探测分节。
2、对方已崩溃且已重新启动：以RST响应。套接口的待处理错误被置为ECONNRESET，套接 口本身则被关闭。
3、对方无任何响应：源自berkeley的TCP发送另外8个探测分节，相隔75秒一个，试图得到一个响应。在发出第一个探测分节11分钟15秒后若仍无响应就放弃。套接口的待处理错误被置为ETIMEOUT，套接口本身则被关闭。如ICMP错误是“host unreachable(主机不可达)”，说明对方主机并没有崩溃，但是不可达，这种情况下待处理错误被置为 EHOSTUNREACH。
         */
        serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        // 是否一有数据就马上发送。
        // TCP_NODELAY选项，就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。
        serverBootstrap.option(ChannelOption.TCP_NODELAY, true);

        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                pipeline.addLast(new MethodResultSerializer());
                pipeline.addLast(new MethodInvoker(serverConf, executorService));
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
        if (serverBootstrap != null) {
            if (serverBootstrap.config().childGroup() != null) {
                serverBootstrap.config().childGroup().shutdownGracefully();
            }
            if (serverBootstrap.config().group() != null) {
                serverBootstrap.config().group().shutdownGracefully();
            }
        }

    }
}
