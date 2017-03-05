package light.rpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import light.rpc.client.async.RequestIdGenerator;
import light.rpc.protocol.Request;
import light.rpc.util.json.JacksonHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Request工厂
 */
public class RequestFactory {

    /**
     * 创建Request对象
     *
     * @param method    方法
     * @param args      方法参数
     * @param async     是否是异步方法
     * @param asyncPort 异步结果接受方端口
     * @return
     */
    public static Request newRequest(Method method, Object[] args, boolean async, int asyncPort) throws JsonProcessingException {
        return newRequest(method.getDeclaringClass(), method, args, async, asyncPort);
    }

    /**
     * 创建Request对象
     *
     * @param clazz     调用类
     * @param method    方法
     * @param args      方法参数
     * @param async     是否是异步方法
     * @param asyncPort 异步结果接受方端口
     * @return
     */
    public static Request newRequest(Class clazz, Method method, Object[] args, boolean async, int asyncPort) throws JsonProcessingException {
        Request request = new Request();
        request.setAsync(async);
        request.setAsyncPort(asyncPort);
        request.setAsyncReqId(RequestIdGenerator.genId());
        request.setIface(clazz.getName());
        request.setMethod(method.getName());
        List<Request.TypeValue> typeValues = new ArrayList<>();
        request.setArgs(typeValues);
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> parameterType = parameterTypes[i];
            typeValues.add(new Request.TypeValue(parameterType.getName(), JacksonHelper.getMapper().writeValueAsString(args[i])));
        }
        return request;
    }
}
