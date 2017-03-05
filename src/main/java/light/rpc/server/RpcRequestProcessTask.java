package light.rpc.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import light.rpc.conf.ServerConf;
import light.rpc.protocol.AsyncCallAckResponse;
import light.rpc.protocol.Request;
import light.rpc.protocol.Response;
import light.rpc.result.Result;
import light.rpc.util.ClassUtil;
import light.rpc.util.CloseableHttpClientFactory;
import light.rpc.util.FullHttpResponseFactory;
import light.rpc.util.InetSocketAddressFactory;
import light.rpc.util.json.JacksonHelper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 处理Rpc请求的任务
 */
@RequiredArgsConstructor
public class RpcRequestProcessTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestProcessTask.class);

    /**
     * 服务端配置
     */
    private final ServerConf serverConf;

    /**
     * netty ChannelHandlerContext,用于发送response
     */
    private final ChannelHandlerContext ctx;

    /**
     * 请求的ByteBuf
     */
    private final ByteBuf byteBuf;

    /**
     * 处理请求
     */
    @Override
    public void run() {
        // 创建Request对象
        String s = byteBuf.toString(CharsetUtil.UTF_8);
        Request request;
        try {
            request = JacksonHelper.getMapper().readValue(s, Request.class);
        } catch (IOException e) {
            ctx.writeAndFlush(FullHttpResponseFactory.BAD_REQUEST);
            logger.warn("invalid request {}", s);
            return;
        }

        // 获取调用的类
        Class<?> clazz;
        try {
            clazz = ClassUtil.forName(request.getIface());
        } catch (ClassNotFoundException e) {
            invokedFailed("class no found", request);
            logger.warn("invalid request, no class found");
            return;
        }

        // 判断调用的类是在Rpc服务方提供的服务类中
        List<Class<?>> interfaces = serverConf.getInterfaces();
        if (!interfaces.contains(clazz)) {
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
            Class<?> type;
            try {
                type = ClassUtil.forName(typeValue.getType());
            } catch (ClassNotFoundException e) {
                invokedFailed("class no found", request);
                logger.warn("class no found");
                return;
            }
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

        // 如果是异步任务,则返回AsyncCallAckResponse
        if (request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(new AsyncCallAckResponse(request.getAsyncReqId(), true, null)));
            logger.debug("send async call ack success");
        }

        // 调用方法
        Result result;
        try {
            Object ret = method.invoke(serviceBean, values.toArray());
            result = Result.invokedSuccess(ret, null, null);
        } catch (InvocationTargetException e) {
            result = Result.invokedSuccess(null, e.getCause(), e.getCause().getClass());
        } catch (IllegalAccessException e) {
            result = Result.invokedFailed(e.getMessage());
            logger.warn(e.getMessage());
        }

        invokedSuccess(result, request);
    }

    /**
     * 调用成功,返回结果给客户端
     *
     * @param result
     * @param request
     */
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

    /**
     * 调用失败,返回结果给客户端
     *
     * @param msg
     * @param request
     */
    private void invokedFailed(String msg, Request request) {
        if (request.isAsync()) {
            ctx.writeAndFlush(genHttpResponse(new AsyncCallAckResponse(request.getAsyncReqId(), false, msg)));
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

    private FullHttpResponse genHttpResponse(AsyncCallAckResponse asyncCallAckResponse) {
        String content = "";
        String contentType = "application/json; charset=utf-8";
        HttpResponseStatus status = HttpResponseStatus.OK;

        try {
            content = JacksonHelper.getMapper().writeValueAsString(asyncCallAckResponse);
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
            if (result.getThrowableType() != null) {
                response.setThrowableType(result.getThrowable().getClass().getName());
            }
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
