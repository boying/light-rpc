package light.rpc.client;

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
    public static Object deserialize(String json, Class<?> retType) throws IOException, ClassNotFoundException {
        if (json == null) {
            throw new IllegalArgumentException("json is null");
        }
        if (retType == null) {
            throw new IllegalArgumentException("retType is null");
        }

        if (retType != void.class) {
            return JacksonHelper.getMapper().readValue(json, retType);
        } else {
            return null;
        }
    }
}

