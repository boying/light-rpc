package conf;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class MethodConf {
    private Method method;
    private int timeoutMillisecond;
}
