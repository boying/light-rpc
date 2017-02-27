package server;

import bean.AsyncResponse;
import bean.Request;
import bean.Result;
import bean.TypeValue;
import conf.ServerConf;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import util.ClassUtil;
import util.json.JacksonHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @Override
    public void run() {
        try {
            innerRun();
        } catch (Exception e) {
            ctx.writeAndFlush(Result.invokedFailed("invoked failed" + e.getMessage()));
        }
    }

    public void innerRun() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        String s = byteBuf.toString(CharsetUtil.UTF_8);
        Request request = JacksonHelper.getMapper().readValue(s, Request.class);

        Class<?> clazz = ClassUtil.forName(request.getIface());
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
    }

    private void invokedFailed(String msg, Request request){
        if(request.isAsync()){
            ctx.writeAndFlush(new AsyncResponse(request.getAsyncReqId(), false, msg));
        }else{
            ctx.writeAndFlush(Result.invokedFailed(msg));
        }
    }
}
