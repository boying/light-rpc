package conf.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class Method {
    private String name;
    private List<String> paramTypes;
    private int timeoutMillisecond;
}
