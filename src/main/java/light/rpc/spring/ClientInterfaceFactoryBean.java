package light.rpc.spring;


import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

/**
 * Created by boying on 2018/8/15.
 */
@Data
public class ClientInterfaceFactoryBean<T> implements FactoryBean<T> {
    private Class<T> clientInterface;
    private T instance;

    @Override
    public T getObject() throws Exception {
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return clientInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


}

