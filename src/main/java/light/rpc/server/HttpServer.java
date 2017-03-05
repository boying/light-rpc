package light.rpc.server;

import io.netty.channel.ChannelHandler;
import light.rpc.conf.ServerConf;
import light.rpc.util.network.SharedChannelHandlerGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Rpc服务提供者的http服务器
 */
@RequiredArgsConstructor
public class HttpServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    /**
     * Rpc服务端配置
     */
    @NonNull
    private final ServerConf serverConf;

    private ExecutorService executorService;
    private static final int WAITING_QUEUE_SIZE = 1000;
    private light.rpc.util.network.HttpServer httpServer;

    @Override
    public void init() {
    }

    @Override
    public void start() {
        executorService = new ThreadPoolExecutor(0, serverConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WAITING_QUEUE_SIZE));


        List<ChannelHandler> handlers = new ArrayList<>();
        handlers.add(new MethodInvoker(serverConf, executorService));

        httpServer = new light.rpc.util.network.HttpServer(serverConf.getPort(), new SharedChannelHandlerGenerator(handlers));
        httpServer.start();

        logger.debug("server start successfully");
    }

    @Override
    public void close() {
        if (httpServer != null) {
            httpServer.close();
        }
    }
}
