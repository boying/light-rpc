package light.rpc.util.network;

import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Created by boying on 17/2/27.
 */
@RequiredArgsConstructor
public class SharedChannelHandlerGenerator implements IChannelHandlerGenerator {
    private final List<ChannelHandler> handlers;

    @Override
    public List<ChannelHandler> gen() {
        return handlers;
    }
}
