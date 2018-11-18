package light.rpc.core;

/**
 * Created by boying on 2018/11/12.
 */
public interface SessionManager {
    void attachSession();

    boolean isInSession();
}
