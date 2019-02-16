package light.rpc.util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by boying on 17/2/18.
 */
public class ServerBootstrapFactory {
    private static final int DEFAULT_BOSS_EVENT_LOOP_SIZE = 1;
    private static final int DEFAULT_WORKER_EVENT_LOOP_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    public static ServerBootstrap newServerBootstrap(boolean epollFirst) {
        return newServerBootstrap(DEFAULT_BOSS_EVENT_LOOP_SIZE, DEFAULT_WORKER_EVENT_LOOP_SIZE, epollFirst);
    }

    public static ServerBootstrap newServerBootstrap(int bossThreads, int workerThreads, boolean epollFirst) {
        if (epollFirst && Epoll.isAvailable()) {
            return newEpollServerBootstrap(bossThreads, workerThreads);
        } else {
            return newNioServerBootstrap(bossThreads, workerThreads);
        }
    }

    private static ServerBootstrap newNioServerBootstrap(int bossThreads, int workerThreads) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(bossThreads);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads);
        return new ServerBootstrap().group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
    }

    private static ServerBootstrap newEpollServerBootstrap(int bossThreads, int workerThreads) {
        EpollEventLoopGroup bossGroup = new EpollEventLoopGroup(bossThreads);
        EpollEventLoopGroup workerGroup = new EpollEventLoopGroup(workerThreads);
        return new ServerBootstrap().group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);
    }
}
