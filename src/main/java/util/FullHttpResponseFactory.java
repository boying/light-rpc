package util;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

/**
 * Created by jiangzhiwen on 17/3/1.
 */
public class FullHttpResponseFactory {
    public static final FullHttpResponse BAD_REQUEST = newFullHttpResponse(HttpResponseStatus.BAD_REQUEST);

    public static FullHttpResponse newFullHttpResponse(HttpResponseStatus status) {
        return newFullHttpResponse(status, null, new byte[]{});
    }

    public static FullHttpResponse newFullHttpResponse(HttpResponseStatus status, String contentType, byte[] content) {
        FullHttpResponse ret = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(content));
        ret.headers().set(HttpHeaderNames.CONTENT_LENGTH, ret.content().readableBytes());
        if (contentType != null) {
            ret.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        }
        return ret;
    }
}
