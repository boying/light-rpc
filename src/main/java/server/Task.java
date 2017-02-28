package server;

import bean.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import conf.ServerConf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import util.ClassUtil;
import util.HttpClientProvider;
import util.InetSocketAddressFactory;
import util.json.JacksonHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/28.
 */
@RequiredArgsConstructor
public class Task implements Runnable {
    private final ServerConf serverConf;
    private final ChannelHandlerContext ctx;
    private final ByteBuf byteBuf;

    private static final FullHttpResponse BAD_REQUEST = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);

    @Override
    public void run() {
        String s = byteBuf.toString(CharsetUtil.UTF_8);
        Request request;
        try {
            request = JacksonHelper.getMapper().readValue(s, Request.class);
        } catch (IOException e) {
            ctx.writeAndFlush(BAD_REQUEST);
            return;
        }

        Class<?> clazz = null;
        try {
            clazz = ClassUtil.forName(request.getIface());
        } catch (ClassNotFoundException e) {
            invokedFailed("class no found", request);
            return;
        }

        List<Class<?>> interfaces = serverConf.getInterfaces();
        if (!interfaces.contains(clazz)) {
            invokedFailed("service interface no registered", request);
            return;
        }

        Object serviceBean = serverConf.getServiceBeanProvider().get(clazz);
        if (serviceBean == null) {
            invokedFailed("service bean no found", request);
            return;
        }

        List<Class<?>> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (TypeValue typeValue : request.getArgs()) {
            Class<?> type = null;
            try {
                type = ClassUtil.forName(typeValue.getType());
            } catch (ClassNotFoundException e) {
                invokedFailed("class no found", request);
                return;
            }
            types.add(type);

            Object o = null;
            try {
                o = JacksonHelper.getMapper().readValue(typeValue.getValue(), type);
            } catch (IOException e) {
                invokedFailed("invalid param value " + typeValue.getValue(), request);
                return;
            }
            values.add(o);
        }

        Method method = null;
        try {
            method = clazz.getMethod(request.getMethod(), Arrays.copyOf(types.toArray(), types.size(), Class[].class));
        } catch (NoSuchMethodException e) {
            invokedFailed("no such method", request);
            return;
        }

        if (request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(new AsyncResponse(request.getAsyncReqId(), true, null)));
        }

        Result result;
        try {
            Object ret = method.invoke(serviceBean, values.toArray());
            result = Result.invokedSuccess(ret, null);
        } catch (InvocationTargetException e) {
            result = Result.invokedSuccess(null, e.getCause());
        } catch (IllegalAccessException e) {
            result = Result.invokedFailed(e.getMessage());
        }

        invokedSuccess(result, request);
    }

    private void invokedSuccess(Result result, Request request) {
        if (!request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(result));
        } else {
            HttpClient httpClient = HttpClientProvider.getHttpClient(
                    InetSocketAddressFactory.get(
                            ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName(),
                            request.getAsyncPort()));

            Response response = result2Response(result);
            String body = "";
            try {
                body = JacksonHelper.getMapper().writeValueAsString(response);
            } catch (JsonProcessingException e) {
                // todo
            }

            HttpPost post = new HttpPost();
            post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            try {
                HttpResponse rsp = httpClient.execute(post);
                int status = rsp.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                }
            } catch (IOException e) {
                // TODO
            }
        }
    }

    private void invokedFailed(String msg, Request request) {
        if (request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(new AsyncResponse(request.getAsyncReqId(), false, msg)));
        } else {
            Result result = new Result();
            result.setInvokedSuccess(false);
            result.setAsync(false);
            result.setErrorMsg(msg);

            ctx.writeAndFlush(genHttpResponse(result));
        }
    }

    private FullHttpResponse genHttpResponse(AsyncResponse asyncResponse) {
        String content = "";
        String contentType = "application/json; charset=utf-8";
        HttpResponseStatus status = HttpResponseStatus.OK;

        try {
            content = JacksonHelper.getMapper().writeValueAsString(asyncResponse);
        } catch (Throwable throwable) {
        }

        FullHttpResponse ret = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(content.getBytes(CharsetUtil.UTF_8)));
        ret.headers().set(org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH, ret.content().readableBytes())
                .set(org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE, contentType);

        return ret;
    }

    private Response result2Response(Result result) {

        Response response = new Response();
        if (result.isAsync()) {
            response.setAsync(true);
            response.setAsyncReqId(result.getAsyncReqId());
        } else {
            response.setAsync(false);
        }
        response.setInvokedSuccess(result.isInvokedSuccess());
        response.setErrorMsg(result.getErrorMsg());
        try {
            response.setResult(JacksonHelper.getMapper().writeValueAsString(result.getResult()));
            response.setThrowable(JacksonHelper.getMapper().writeValueAsString(result.getThrowable()));
        } catch (Throwable throwable) {
            response.setInvokedSuccess(false);
            response.setErrorMsg("server error");
        }

        return response;
    }

    private FullHttpResponse genHttpResponse(Result result) {
        String content = "";
        String contentType = "application/json; charset=utf-8";
        HttpResponseStatus status = HttpResponseStatus.OK;

        Response response = result2Response(result);
        try {
            content = JacksonHelper.getMapper().writeValueAsString(response);
        } catch (JsonProcessingException ignored) {
        }

        FullHttpResponse ret = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(content.getBytes(CharsetUtil.UTF_8)));
        ret.headers().set(org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH, ret.content().readableBytes())
                .set(org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE, contentType);

        return ret;
    }
}
