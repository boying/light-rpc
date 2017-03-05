package light.rpc.client.async;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import light.rpc.util.network.HttpServer;
import light.rpc.util.network.SharedChannelHandlerGenerator;

import java.util.Arrays;
import java.util.Collections;

/**
 * 异步调用结果接受的服务器
 */
@RequiredArgsConstructor
public class AsyncCallServer {
    /**
     * 服务器监听端口
     */

    private final int port;

    /**
     * 异步调用Future容器
     */
    @NonNull
    private final AsyncCallFutureContainer asyncCallFutureContainer;

    /**
     * 内部Http服务器
     */
    private HttpServer httpServer;

    /**
     * 初始化
     */
    public void init() {
    }

    /**
     * 启动
     */
    public void start() {
        httpServer = new HttpServer(port,
                new SharedChannelHandlerGenerator(Collections.singletonList(new ResponseHandler(asyncCallFutureContainer))));
        httpServer.start();
    }

    /**
     * 关闭
     */
    public void close() {
        if (httpServer != null) {
            httpServer.close();
        }
    }
}
