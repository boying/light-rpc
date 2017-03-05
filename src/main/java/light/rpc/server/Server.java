package light.rpc.server;

/**
 * Rpc服务端接口
 */
public interface Server {
    /**
     * 初始化
     */
    void init();

    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void close();
}
