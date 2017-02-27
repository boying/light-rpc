package client.async;

import bean.Response;
import bean.Result;
import client.Json2ResultDeserializer;
import exception.ClientException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import util.json.JacksonHelper;

/**
 * Created by jiangzhiwen on 17/2/28.
 */
@RequiredArgsConstructor
public class ResponseHandler extends ChannelInboundHandlerAdapter {
    private final AsyncCallFutureContainer asyncCallFutureContainer;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest httpRequest = (FullHttpRequest) msg;
        String json = httpRequest.content().toString(CharsetUtil.UTF_8);

        Response response = JacksonHelper.getMapper().readValue(json, Response.class);
        AsyncCallFuture<?> future = asyncCallFutureContainer.getByReqId(response.getAsyncReqId());
        if (future == null) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            return;
        }

        try {
            Result result = Json2ResultDeserializer.deserialize(json, future.getMethod().getReturnType());
            asyncCallFutureContainer.discardAsyncCallFuture(result);
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        } catch (Exception e) {
            asyncCallFutureContainer.discardAsyncCallFuture(future, new ClientException(e));
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        }
    }
}
