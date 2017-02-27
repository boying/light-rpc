package client.async;

import lombok.RequiredArgsConstructor;
import util.network.HttpServer;
import util.network.SharedChannelHandlerGenerator;

import java.util.Arrays;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
@RequiredArgsConstructor
public class AsyncCallServer {
    private final int port;
    private final AsyncCallFutureContainer asyncCallFutureContainer;
    private HttpServer httpServer;

    public void init() {

    }

    public void start() {
        httpServer = new HttpServer(port, new SharedChannelHandlerGenerator(Arrays.asList(new ResponseHandler(asyncCallFutureContainer))));
        httpServer.start();
    }

    public void close() {
        if (httpServer != null) {
            httpServer.close();
        }
    }
}
