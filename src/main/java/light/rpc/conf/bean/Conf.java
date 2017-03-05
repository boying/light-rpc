package light.rpc.conf.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/11.
 */
@Data
public class Conf {
    private Common common;
    private List<Client> clients;
    private Server server;
}
