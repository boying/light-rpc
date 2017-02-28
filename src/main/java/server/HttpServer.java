package server;

import conf.ServerConf;
import io.netty.channel.ChannelHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import util.network.SharedChannelHandlerGenerator;

import java.util.ArrayList;
import java.util.List;
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
    private util.network.HttpServer httpServer;

    @Override
    public void init() {
    }

    @Override
    public void start() {
        executorService = new ThreadPoolExecutor(0, serverConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WAITING_QUEUE_SIZE));


        List<ChannelHandler> handlers = new ArrayList<>();
        handlers.add(new MethodInvoker(serverConf, executorService));

        httpServer = new util.network.HttpServer(serverConf.getPort(), new SharedChannelHandlerGenerator(handlers));

        httpServer.start();
    }

    @Override
    public void close() {
        if (httpServer != null) {
            httpServer.close();
        }
    }
}
