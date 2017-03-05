package light.rpc.core;

/**
 * Rpc服务对象的提供者,Rpc的服务方需要提供此接口的实现
 */
public interface ServiceBeanProvider {
    /**
     * 根据class类型,获取class对应的服务对象
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T get(Class<T> clazz);
}
