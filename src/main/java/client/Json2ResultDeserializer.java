package client;

import bean.Response;
import bean.Result;
import util.json.JacksonHelper;

import java.io.IOException;

/**
 * Created by jiangzhiwen on 17/2/28.
 */
public class Json2ResultDeserializer {
    public static Result deserialize(String json, Class<?> retType) throws IOException {
        Response response = deserialize2Response(json);

        Result ret = new Result();
        ret.setInvokedSuccess(response.isInvokedSuccess());
        Object result;
        if (retType != void.class) {
            result = JacksonHelper.getMapper().readValue(response.getResult(), retType);
        } else {
            result = "null";
        }
        ret.setResult(result);

        Throwable throwable = JacksonHelper.getMapper().readValue(response.getThrowable(), Throwable.class);
        ret.setThrowable(throwable);
        ret.setErrorMsg(response.getErrorMsg());
        return ret;

    }

    private static Response deserialize2Response(String json) {
        try {
            return JacksonHelper.getMapper().readValue(json, Response.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

