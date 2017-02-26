package client;

import bean.Request;
import bean.Response;
import bean.Result;
import bean.TypeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import conf.ServerProviderConf;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server_provider.IServerProvider;
import util.HttpClientProvider;
import util.json.JacksonHelper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
public class Task implements Callable<Result> {
    private static Logger logger = LoggerFactory.getLogger(Task.class);

    private static final JavaType RESPONSE_JAVA_TYPE = JacksonHelper.genJavaType(Response.class);
    private static final Map<Class, JavaType> clazzJavaTypeMap = new ConcurrentHashMap<>();

    private IServerProvider hostProvider;
    private Method method;
    private Object[] args;

    public Task(IServerProvider hostProvider, Method method, Object[] args) {
        this.hostProvider = hostProvider;
        this.method = method;
        this.args = args;
    }

    @Override
    public Result call() throws Exception {
        Request request = RequestFactory.newRequest(method, args, false, 0);
        String body = RequestJsonSerializer.serialize(request);
        logger.debug("request is {}", body);
        String rsp = RequestJsonSender.send(hostProvider, body);
        logger.debug("response is {}", rsp);
        Response response = deserialize(rsp);
        return deserializeResult(response);
    }

    private Response deserialize(String json) {
        try {
            return JacksonHelper.getMapper().readValue(json, RESPONSE_JAVA_TYPE);
            /* TODO why not use
            return JacksonHelper.getMapper().readValue(json, Response.class);
            */
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Result deserializeResult(Response response) throws IOException {
        Result ret = new Result();
        ret.setInvokedSuccess(response.isInvokedSuccess());
        Object result;
        if(method.getReturnType() != void.class) {
            result = JacksonHelper.getMapper().readValue(response.getResult(), method.getReturnType());
        }else{
            result = "null";
        }
        ret.setResult(result);

        Throwable throwable = JacksonHelper.getMapper().readValue(response.getThrowable(), Throwable.class);
        ret.setThrowable(throwable);
        ret.setErrorMsg(response.getErrorMsg());
        return ret;
    }
}
