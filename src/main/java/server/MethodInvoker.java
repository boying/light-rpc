package server;

import bean.Request;
import bean.Response;
import bean.Result;
import bean.TypeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import conf.ServerConf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.jboss.netty.handler.codec.http.*;
import util.ClassUtil;
import util.json.JacksonHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
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

        executorService.submit(() -> {
            try {
                String s = byteBuf.toString(CharsetUtil.UTF_8);
                Request request = JacksonHelper.getMapper().readValue(s, Request.class);

                Class<?> clazz = ClassUtil.forName(request.getIface());
                List<Class<?>> interfaces = serverConf.getInterfaces();
                if (!interfaces.contains(clazz)) {
                    ctx.writeAndFlush(Result.invokedFailed("service interface no registered"));
                    return;
                }

                Object serviceBean = serverConf.getServiceBeanProvider().get(clazz);
                if (serviceBean == null) {
                    ctx.writeAndFlush(Result.invokedFailed("service bean no found"));
                    return;
                }

                List<Class<?>> types = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                for (TypeValue typeValue : request.getArgs()) {
                    Class<?> type = ClassUtil.forName(typeValue.getType());
                    types.add(type);

                    Object o = JacksonHelper.getMapper().readValue(typeValue.getValue(), type);
                    values.add(o);
                }

                Method method = clazz.getMethod(request.getMethod(), Arrays.copyOf(types.toArray(), types.size(), Class[].class));

                try {
                    Object ret = method.invoke(serviceBean, values.toArray());
                    ctx.writeAndFlush(Result.invokedSuccess(ret, null));
                } catch (InvocationTargetException e) {
                    ctx.writeAndFlush(Result.invokedSuccess(null, e.getCause()));
                }
            } catch (Exception e) {
                ctx.writeAndFlush(Result.invokedFailed("invoked failed" + e.getMessage()));
            }
        });

    }
}
