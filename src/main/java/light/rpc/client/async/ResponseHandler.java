package light.rpc.client.async;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import light.rpc.client.Json2ResultDeserializer;
import light.rpc.exception.ClientException;
import light.rpc.protocol.Response;
import light.rpc.result.Result;
import light.rpc.util.FullHttpResponseFactory;
import light.rpc.util.json.JacksonHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiangzhiwen on 17/2/28.
 */
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class ResponseHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private final AsyncCallFutureContainer asyncCallFutureContainer;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        String json = httpRequest.content().toString(CharsetUtil.UTF_8);

        Response response = JacksonHelper.getMapper().readValue(json, Response.class);
        logger.debug("response is {}", response);

        AsyncCallFuture<?> future = asyncCallFutureContainer.getByReqId(response.getAsyncReqId());
        if (future == null) {
            logger.warn("no future found by reqId {}", response.getAsyncReqId());
            ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.NOT_FOUND));
            return;
        }

        try {
            Result result = Json2ResultDeserializer.deserialize(json, future.getMethod().getReturnType());
            asyncCallFutureContainer.discardAsyncCallFuture(result);
            ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.OK));
            logger.debug("get async result success, result is {}", result);
        } catch (Exception e) {
            logger.warn("get async result failed, ", e);
            asyncCallFutureContainer.discardAsyncCallFuture(future, new ClientException(e));
            ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.BAD_REQUEST));
        }
    }
}
