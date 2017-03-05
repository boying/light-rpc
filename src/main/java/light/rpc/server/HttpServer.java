package light.rpc.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.ChannelHandler;
import light.rpc.conf.ServerConf;
import light.rpc.util.network.SharedChannelHandlerGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
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
    private light.rpc.util.network.HttpServer httpServer;

    @Override
    public void init() {
    }

    @Override
    public void start() {
        String threadNameFormat = "rpc_server-" + serverConf.getAppId() + "-thread_pool-thread-%d";
        executorService = new ThreadPoolExecutor(0, serverConf.getThreadPoolSize(), 1, TimeUnit.MINUTES, new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build());

        List<ChannelHandler> handlers = new ArrayList<>();
        handlers.add(new RpcRequestHandler(serverConf, executorService));

        httpServer = new light.rpc.util.network.HttpServer(serverConf.getPort(), new SharedChannelHandlerGenerator(handlers));
        httpServer.start();

        logger.debug("server start successfully");
    }

    @Override
    public void close() {
        if (httpServer != null) {
            httpServer.close();
        }
        executorService.shutdown();
    }
}
