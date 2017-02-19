package server;

import bean.Request;
import conf.ServerConf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;
import util.json.JacksonHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
public class HttpServerMethodInvoker extends ChannelInboundHandlerAdapter {
    private final ServerConf serverConf;
    private ExecutorService executorService;

    public HttpServerMethodInvoker(ServerConf serverConf) {
        this.serverConf = serverConf;
        executorService = Executors.newFixedThreadPool(serverConf.getThreadPoolSize());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object[] msgs = (Object[]) msg;
        HttpRequest httpRequest = (HttpRequest) msgs[0];
        ByteBuf byteBuf = (ByteBuf) msgs[1];

        executorService.submit(() -> {
            try {
                String s = byteBuf.toString(CharsetUtil.UTF_8);
                Request request = JacksonHelper.getMapper().readValue(s, Request.class);

                Class<?> clazz = Class.forName(request.getIface());
                List<Class<?>> interfaces = serverConf.getInterfaces();
                if (!interfaces.contains(clazz)) {
                    ctx.writeAndFlush(Result.invokedFailed("service interface no registed"));
                    return;
                }

                Object serviceBean = serverConf.getServiceBeanProvider().get(clazz);
                if (serviceBean == null) {
                    ctx.writeAndFlush(Result.invokedFailed("service bean no found"));
                    return;
                }

                List<Class<?>> types = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                for (Map<String, String> typeValueMap : request.getArgs()) {
                    for (String key : typeValueMap.keySet()) {
                        Class<?> type = Class.forName(key);
                        types.add(type);

                        String val = typeValueMap.get(key);
                        Object o = JacksonHelper.getMapper().readValue(val, type);
                        values.add(o);
                    }
                }

                Method method = clazz.getMethod(request.getMethod(), (Class<?>[]) types.toArray());

                try {
                    Object ret = method.invoke(serviceBean, values.toArray());
                    ctx.writeAndFlush(Result.invokedSuccess(ret, null));
                }catch (InvocationTargetException e){
                    ctx.writeAndFlush(Result.invokedSuccess(null, e.getCause()));
                }
            } catch (Exception e) {
                ctx.writeAndFlush(Result.invokedFailed("invoked failed" + e.getMessage()));
            }
        });

    }
}
