package server;

/**
 * Created by jiangzhiwen on 17/2/18.
 */
public interface ServiceBeanProvider {
    <T> T get(Class<T> clazz);
}
