package light.rpc.conf.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Created by jiangzhiwen on 17/2/12.
 */
@Data
@AllArgsConstructor
@ToString
public class ServerProvider {
    private String ip;
    private int port;
}
