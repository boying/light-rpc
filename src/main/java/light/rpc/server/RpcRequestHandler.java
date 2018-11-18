package light.rpc.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import light.rpc.conf.Config;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

/**
 * Rpc请求处理器
 */
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class RpcRequestHandler extends ChannelInboundHandlerAdapter {
    /**
     * 服务端配置
     */
    @NonNull
    private final Config.Server serverConf;

    /**
     * 服务端线程池
     */
    @NonNull
    private final ExecutorService executorService;

    /**
     * 处理请求
     * 创建一个处理任务,提交至线程池
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        ByteBuf byteBuf = httpRequest.content();

        executorService.submit(new RpcRequestProcessTask(serverConf, ctx, byteBuf));
    }
}
