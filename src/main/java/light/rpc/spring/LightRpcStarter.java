package light.rpc.spring;

import light.rpc.core.RpcContext;
import light.rpc.core.ServiceBeanProvider;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by boying on 2018/11/8.
 */
@Data
public class LightRpcStarter implements BeanDefinitionRegistryPostProcessor, ApplicationListener {
    private String configPath = "light_rpc_config.json";
    private ServiceBeanProvider serviceBeanProvider;
    private RpcContext rpcContext;
    private String name;
    private static AtomicInteger id = new AtomicInteger(1);
    public static String BEAN_PREFIX = "lightRpcClient_";

    public LightRpcStarter(String configPath, ServiceBeanProvider serviceBeanProvider) {
        this.configPath = configPath;
        this.serviceBeanProvider = serviceBeanProvider;
        init();
    }

    private void init() {
        rpcContext = new RpcContext(configPath, serviceBeanProvider);
        try {
            rpcContext.start(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerClientInterfaceToSpringContext(registry);
    }

    private void registerClientInterfaceToSpringContext(BeanDefinitionRegistry registry) {
        for (Map.Entry<Class, Object> classObjectEntry : rpcContext.getClassProxyMap().entrySet()) {
            registerClientInterfaceToSpringContext(classObjectEntry.getKey(), classObjectEntry.getValue(), registry);
        }
    }

    private void registerClientInterfaceToSpringContext(Class clazz, Object instance, BeanDefinitionRegistry registry) {
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(ClientInterfaceFactoryBean.class)
                .addPropertyValue("clientInterface", clazz)
                .addPropertyValue("instance", instance)
                .getBeanDefinition();
        registry.registerBeanDefinition(genBeanName(clazz), beanDefinition);
    }

    private String genBeanName(Class clazz){
        return String.format("%s%s_%s", BEAN_PREFIX,
                StringUtils.isEmpty(name) ? "" + id.getAndIncrement() : name,
                clazz.getSimpleName());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!(event instanceof ContextRefreshedEvent)) {
            return;
        }

        try {
            rpcContext.registerServiceToRegistry();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
