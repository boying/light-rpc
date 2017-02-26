package client;

import bean.Request;
import com.fasterxml.jackson.core.JsonProcessingException;
import util.json.JacksonHelper;

/**
 * Created by jiangzhiwen on 17/2/26.
 */
public class RequestJsonSerializer {
    public static String serialize(Request request) {
        try {
            return JacksonHelper.getMapper().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
