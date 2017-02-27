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

        executorService.submit(new Task(serverConf, ctx, byteBuf));
    }
}
