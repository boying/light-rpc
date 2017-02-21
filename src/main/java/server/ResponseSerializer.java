package server;

import bean.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import util.json.JacksonHelper;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
public class ResponseSerializer extends ChannelOutboundHandlerAdapter{
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(!(msg instanceof Result)){
            ctx.write(msg);
        }

        Result result = (Result) msg;
        FullHttpResponse httpResponse = genHttpResponse(result);
        ctx.writeAndFlush(httpResponse);
    }

    private FullHttpResponse genHttpResponse(Result result){
        String content = "";
        String contentType = "application/json; charset=utf-8";
        HttpResponseStatus status = HttpResponseStatus.OK;
        try {
            content = JacksonHelper.getMapper().writeValueAsString(result);
        }catch (Throwable throwable){
            Result rst = new Result();
            rst.setInvokedSuccess(false);
            rst.setErrorMsg("serialize result error, " + throwable.getMessage());
            try {
                content = JacksonHelper.getMapper().writeValueAsString(rst);
            } catch (JsonProcessingException ignored) {
            }
        }

        FullHttpResponse ret = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(content.getBytes(CharsetUtil.UTF_8)));
        ret.headers().set(HttpHeaders.Names.CONTENT_LENGTH, ret.content().readableBytes())
                .set(HttpHeaders.Names.CONTENT_TYPE, contentType);

        return ret;
    }
}
