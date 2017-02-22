package util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiangzhiwen on 17/2/23.
 */
public class ClassUtil {
    private static Map<String, Class<?>> primitiveTypeNameClassMap = new HashMap<>();

    static {
        primitiveTypeNameClassMap.put(boolean.class.getName(), boolean.class);
        primitiveTypeNameClassMap.put(byte.class.getName(), byte.class);
        primitiveTypeNameClassMap.put(char.class.getName(), char.class);
        primitiveTypeNameClassMap.put(double.class.getName(), double.class);
        primitiveTypeNameClassMap.put(float.class.getName(), float.class);
        primitiveTypeNameClassMap.put(int.class.getName(), int.class);
        primitiveTypeNameClassMap.put(long.class.getName(), long.class);
        primitiveTypeNameClassMap.put(short.class.getName(), short.class);
        primitiveTypeNameClassMap.put(void.class.getName(), void.class);
    }

    public static Class<?> forName(String className) throws ClassNotFoundException {
        if (primitiveTypeNameClassMap.containsKey(className)) {
            return primitiveTypeNameClassMap.get(className);
        }
        return Class.forName(className);
    }
}
