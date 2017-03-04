package server;

import bean.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import conf.ServerConf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ClassUtil;
import util.CloseableHttpClientFactory;
import util.FullHttpResponseFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    private final ServerConf serverConf;
    private final ChannelHandlerContext ctx;
    private final ByteBuf byteBuf;

    @Override
    public void run() {
        String s = byteBuf.toString(CharsetUtil.UTF_8);
        Request request;
        try {
            request = JacksonHelper.getMapper().readValue(s, Request.class);
        } catch (IOException e) {
            ctx.writeAndFlush(FullHttpResponseFactory.BAD_REQUEST);
            logger.warn("invalid request {}", s);
            return;
        }

        Class<?> clazz = null;
        try {
            clazz = ClassUtil.forName(request.getIface());
        } catch (ClassNotFoundException e) {
            invokedFailed("class no found", request);
            logger.warn("invalid request, no class found");
            return;
        }

        List<Class<?>> interfaces = serverConf.getInterfaces();
        if (!interfaces.contains(clazz)) {
            invokedFailed("service interface no registered", request);
            logger.warn("invalid request, service interface no registered");
            return;
        }

        Object serviceBean = serverConf.getServiceBeanProvider().get(clazz);
        if (serviceBean == null) {
            invokedFailed("service bean no found", request);
            logger.warn("service bean no found");
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
                logger.warn("class no found");
                return;
            }
            types.add(type);

            Object o = null;
            try {
                o = JacksonHelper.getMapper().readValue(typeValue.getValue(), type);
            } catch (IOException e) {
                invokedFailed("invalid param value " + typeValue.getValue(), request);
                logger.warn("invalid param value {}", typeValue.getValue());
                return;
            }
            values.add(o);
        }

        Method method = null;
        try {
            method = clazz.getMethod(request.getMethod(), Arrays.copyOf(types.toArray(), types.size(), Class[].class));
        } catch (NoSuchMethodException e) {
            invokedFailed("no such method " + request.getMethod(), request);
            logger.warn("no such method " + request.getMethod());
            return;
        }

        if (request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(new AsyncResponse(request.getAsyncReqId(), true, null)));
            logger.debug("send async call ack success");
        }

        Result result;
        try {
            Object ret = method.invoke(serviceBean, values.toArray());
            result = Result.invokedSuccess(ret, null);
        } catch (InvocationTargetException e) {
            result = Result.invokedSuccess(null, e.getCause());
        } catch (IllegalAccessException e) {
            result = Result.invokedFailed(e.getMessage());
            logger.warn(e.getMessage());
        }

        invokedSuccess(result, request);
    }

    private void invokedSuccess(Result result, Request request) {
        if (!request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(result));
            logger.debug("send sync call result success");
        } else {
            String hostName = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
            int port = request.getAsyncPort();
            CloseableHttpClient httpClient = CloseableHttpClientFactory.getCloseableHttpClient(
                    InetSocketAddressFactory.get(hostName, port));


            result.setAsync(true);
            result.setAsyncReqId(request.getAsyncReqId());
            Response response = result2Response(result);
            String body = "";
            try {
                body = JacksonHelper.getMapper().writeValueAsString(response);
            } catch (JsonProcessingException e) {
                logger.warn(e.getMessage());
            }

            HttpPost post = new HttpPost("http://" + hostName + ":" + port);
            post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse rsp = httpClient.execute(post)) {
                int status = rsp.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                    logger.warn("send async call result success, response is {}, but receive no OK ack");
                } else {
                    logger.debug("send async call result success, response is {}", body);
                }
            } catch (Exception e) {
                logger.warn("send async call result failed, ", e);
            }
        }
    }

    private void invokedFailed(String msg, Request request) {
        if (request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(new AsyncResponse(request.getAsyncReqId(), false, msg)));
            logger.debug("send async call ack, invoke failed");
        } else {
            Result result = new Result();
            result.setInvokedSuccess(false);
            result.setAsync(false);
            result.setErrorMsg(msg);

            ctx.writeAndFlush(genHttpResponse(result));
            logger.debug("send sync call ack, invoke failed");
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

        FullHttpResponse ret = FullHttpResponseFactory.newFullHttpResponse(status, contentType, content.getBytes(CharsetUtil.UTF_8));
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

        FullHttpResponse ret = FullHttpResponseFactory.newFullHttpResponse(status, contentType, content.getBytes(CharsetUtil.UTF_8));

        return ret;
    }
}
