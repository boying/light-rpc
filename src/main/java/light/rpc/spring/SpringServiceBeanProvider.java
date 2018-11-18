package light.rpc.spring;

import light.rpc.core.ServiceBeanProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by boying on 2018/11/12.
 */
public class SpringServiceBeanProvider implements ServiceBeanProvider, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public <T> T get(Class<T> clazz) {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(clazz);
        for (String name : beanNamesForType) {
            if (!name.startsWith(LightRpcStarter.BEAN_PREFIX)) {
                return applicationContext.getBean(name, clazz);
            }
        }
        return null;

        // TODO
        // return applicationContext.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
