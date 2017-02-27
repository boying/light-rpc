package util.network;

import io.netty.channel.ChannelHandler;

import java.util.List;

/**
 * Created by jiangzhiwen on 17/2/27.
 */
public interface IChannelHandlerGenerator {
    List<ChannelHandler> gen();
}
