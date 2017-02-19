package conf;


import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class InterfaceConf {
    private Class clazz;
    private List<MethodConf> methodConfs;

}
