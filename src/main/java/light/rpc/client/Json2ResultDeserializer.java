package light.rpc.client;

import light.rpc.protocol.Response;
import light.rpc.result.Result;
import light.rpc.util.json.JacksonHelper;

import java.io.IOException;

/**
 * Json反序列化为Result对象
 */
public class Json2ResultDeserializer {

    /**
     * 将json串反序列化为Result对象
     *
     * @param json    jsonc串
     * @param retType
     * @return
     * @throws IOException
     */
    public static Result deserialize(String json, Class<?> retType) throws IOException {
        if (json == null) {
            throw new IllegalArgumentException("json is null");
        }
        if (retType == null) {
            throw new IllegalArgumentException("retType is null");
        }

        Response response = JacksonHelper.getMapper().readValue(json, Response.class);

        Result ret = new Result();
        ret.setAsyncReqId(response.getAsyncReqId());
        ret.setAsync(response.isAsync());
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
}

