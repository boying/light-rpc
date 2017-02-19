package task;

import bean.Request;
import bean.Response;
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
public class JsonTask implements Callable{
    private static final JavaType RESPONSE_JAVA_TYPE = JacksonHelper.genJavaType(Response.class);
    private static final Map<Class, JavaType> clazzJavaTypeMap = new ConcurrentHashMap<>();

    private HttpClient httpClient;
    private IServerProvider hostProvider;
    private Method method;
    private Object[] args;

    public JsonTask(IServerProvider hostProvider, Method method, Object[] args) {
        this.hostProvider = hostProvider;
        this.method = method;
        this.args = args;
    }

    @Override
    public Object call() throws Exception {
        Request request = genRequest();
        String body = serialize(request);
        String rsp = send(body);
        Response response = deserialize(rsp);

        if(response.getThrowable() != null){
            throw new RuntimeException(response.getThrowable());
        }

        return deserializeResult(response.getResult());

    }

    private Request genRequest(){
        Request request = new Request();
        request.setIface(method.getDeclaringClass().getName());
        request.setMethod(method.getName());
        List<Map<String, String>> paramMapList = new ArrayList<>();
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; ++i){
            Class<?> parameterType = parameterTypes[i];
            HashMap<String, String> param = new HashMap<>();
            try {
                param.put(parameterType.getName(), JacksonHelper.getMapper().writeValueAsString(args[i]));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            paramMapList.add(param);
        }
        return null;
    }

    private String serialize(Request request){
        try {
            return JacksonHelper.getMapper().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String send(String body){
        InetSocketAddress serverProviderAddress = hostProvider.get();
        httpClient = HttpClientProvider.getHttpClient(serverProviderAddress);
        HttpPost post = new HttpPost(genHttpPostUrl(serverProviderAddress));
        post.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        try {
            HttpResponse rsp = httpClient.execute(post);
            int status = rsp.getStatusLine().getStatusCode();
            if(status != HttpStatus.SC_OK){
                throw new RuntimeException();
            }
            return EntityUtils.toString(rsp.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String genHttpPostUrl(InetSocketAddress address){
        return "http://" + address;
    }

    private Response deserialize(String json){
        try {
            return JacksonHelper.getMapper().readValue(json, RESPONSE_JAVA_TYPE);
            /* TODO why not use
            return JacksonHelper.getMapper().readValue(json, Response.class);
            */
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Object deserializeResult(String rst){
        Class<?> returnType = method.getReturnType();
        if(returnType == void.class || returnType == Void.class){
            return null;
        }

        JavaType javaType = clazzJavaTypeMap.get(returnType);
        if(javaType == null){
            javaType = JacksonHelper.genJavaType(returnType);
            clazzJavaTypeMap.put(returnType, javaType);
        }

        try {
            return JacksonHelper.getMapper().readValue(rst, javaType);
            /* TODO why not use
            return JacksonHelper.getMapper().readValue(rst,method.getReturnType());
            */
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
