package light.rpc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import light.rpc.conf.Config;
import light.rpc.protocol.Request;
import light.rpc.util.ClassUtil;
import light.rpc.util.FullHttpResponseFactory;
import light.rpc.util.json.JacksonHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 处理Rpc请求的任务
 */
@RequiredArgsConstructor
public class RpcRequestProcessTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestProcessTask.class);

    /**
     * 服务端配置
     */
    private final Config.Server serverConf;

    /**
     * netty ChannelHandlerContext,用于发送response
     */
    private final ChannelHandlerContext ctx;

    /**
     * 请求的ByteBuf
     */
    private final FullHttpRequest httpRequest;

    /**
     * 处理请求
     */
    @Override
    public void run() {
        // 创建Request对象
        String s = httpRequest.content().toString(CharsetUtil.UTF_8);
        Request request;
        try {
            request = JacksonHelper.getMapper().readValue(s, Request.class);
        } catch (IOException e) {
            ctx.writeAndFlush(FullHttpResponseFactory.BAD_REQUEST);
            logger.warn("invalid request {}", s);
            return;
        }

        // 获取调用的类
        Class<?> clazz = ClassUtil.forName(request.getIface());

        // 判断调用的类是在Rpc服务方提供的服务类中
        List<Class> interfaces = serverConf.getInterfaces();
        if (!interfaces.contains(clazz)) {
            // TODO notFound
            invokedFailed("service interface no registered", request);
            logger.warn("invalid request, service interface no registered");
            return;
        }

        // 查找实现类对象
        Object serviceBean = serverConf.getServiceBeanProvider().get(clazz);
        if (serviceBean == null) {
            invokedFailed("service no found", request);
            logger.warn("service found");
            return;
        }

        // 获取调用参数
        List<Class<?>> types = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Request.TypeValue typeValue : request.getArgs()) {
            Class<?> type = ClassUtil.forName(typeValue.getType());
            types.add(type);

            Object o;
            try {
                o = JacksonHelper.getMapper().readValue(typeValue.getValue(), type);
            } catch (IOException e) {
                invokedFailed("invalid param value " + typeValue.getValue(), request);
                logger.warn("invalid param value {}", typeValue.getValue());
                return;
            }
            values.add(o);
        }

        // 获取调用方法
        Method method;
        try {
            method = clazz.getMethod(request.getMethod(), Arrays.copyOf(types.toArray(), types.size(), Class[].class));
        } catch (NoSuchMethodException e) {
            invokedFailed("no such method " + request.getMethod(), request);
            logger.warn("no such method " + request.getMethod());
            return;
        }

        try {
            Object ret = method.invoke(serviceBean, values.toArray());
            invokedSuccess(ret);
        } catch (InvocationTargetException e) {
            serverError();
        } catch (IllegalAccessException e) {
            badRequest();
        } catch (IllegalArgumentException e) {
            badRequest();
        } catch (Exception e) {
            serverError();
        }
    }


    /**
     * 调用成功,返回结果给客户端
     *
     * @param result
     */
    private void invokedSuccess(Object result) {
        ctx.writeAndFlush(genHttpResponse(result));
        logger.debug("send sync call result success");
    }

    private void notFound() {
        ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.NOT_FOUND));
    }

    private void forbidden() {
        ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.FORBIDDEN));
    }

    private void serverError() {
        ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
    }

    private void badRequest() {
        ctx.writeAndFlush(FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.BAD_REQUEST));
    }

    private Iterable<Map.Entry<String, String>> getSession() {
        return httpRequest.headers();
    }


    /**
     * 调用失败,返回结果给客户端
     *
     * @param msg
     * @param request
     */
    private void invokedFailed(String msg, Request request) {
        serverError();
    }


    private FullHttpResponse genHttpResponse(Object result) {
        String content = "";
        String contentType = "application/json; charset=utf-8";

        try {
            content = JacksonHelper.getMapper().writeValueAsString(result);
        } catch (JsonProcessingException ignored) {
            // TODO
        }

        return FullHttpResponseFactory.newFullHttpResponse(HttpResponseStatus.OK,
                contentType, content.getBytes(CharsetUtil.UTF_8));
    }
}
