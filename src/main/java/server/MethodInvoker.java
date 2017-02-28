package server;

import conf.ServerConf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.ExecutorService;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
@ChannelHandler.Sharable
public class MethodInvoker extends ChannelInboundHandlerAdapter {
    private final ServerConf serverConf;
    private ExecutorService executorService;

    public MethodInvoker(ServerConf serverConf, ExecutorService executorService) {
        this.serverConf = serverConf;
        this.executorService = executorService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        ByteBuf byteBuf = httpRequest.content();

        executorService.submit(new Task(serverConf, ctx, byteBuf));
    }
}
