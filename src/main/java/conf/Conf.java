package conf;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class Conf {
    private CommonConf commonConf;
    private List<ClientConf> clientConfs;
    private ServerConf serverConf;
}
