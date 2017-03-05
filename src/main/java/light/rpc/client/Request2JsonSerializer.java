package light.rpc.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import light.rpc.protocol.Request;
import light.rpc.util.json.JacksonHelper;

/**
 * 将Request对象序列化为Json串
 */
public class Request2JsonSerializer {

    /**
     * 将Request对象序列化为Json串
     *
     * @param request request对象
     * @return
     * @throws JsonProcessingException
     */
    public static String serialize(Request request) throws JsonProcessingException {
        if (request == null) {
            throw new RuntimeException("request is null");
        }
        return JacksonHelper.getMapper().writeValueAsString(request);
    }
}
