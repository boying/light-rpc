package client;

import bean.Request;
import bean.TypeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import util.json.JacksonHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/27.
 */
public class RequestFactory {

    public static Request newRequest(Method method, Object[] args, boolean async, int asyncPort) {
        return newRequest(method.getDeclaringClass(), method, args, async, asyncPort);
    }

        public static Request newRequest(Class clazz, Method method, Object[] args, boolean async, int asyncPort) {
        Request request = new Request();
        request.setAsync(async);
        request.setAsyncPort(asyncPort);
        request.setAsyncReqId(RequestIdGenertor.genId());
        request.setIface(clazz.getName());
        request.setMethod(method.getName());
        List<TypeValue> typeValues = new ArrayList<>();
        request.setArgs(typeValues);
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> parameterType = parameterTypes[i];
            try {
                typeValues.add(new TypeValue(parameterType.getName(), JacksonHelper.getMapper().writeValueAsString(args[i])));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return request;
    }
}
