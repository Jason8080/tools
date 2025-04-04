package cn.gmlee.tools.base.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring基础工具
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class IocUtil {


    /**
     * 主动向Spring容器中注册bean
     *
     * @param <T>                the type parameter
     * @param applicationContext Spring容器
     * @param clazz              注册的bean的类性
     * @param args               构造方法的必要参数，顺序和类型要求和clazz中定义的一致
     * @return 返回注册到容器中的bean对象 t
     */
    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, Class<T> clazz, Object... args) {
        AssertUtil.notNull(clazz, "注册对象类型是空");
        return registerBean(applicationContext, clazz.getSimpleName(), clazz, args);
    }

    public static <T> T registerBean(ConfigurableApplicationContext applicationContext, String name, Class<T> clazz, Object... args) {
        AssertUtil.notNull(clazz, "注册对象类型是空");
        if (applicationContext.containsBean(name)) {
            Object bean = applicationContext.getBean(name);
            if (bean.getClass().isAssignableFrom(clazz)) {
                return (T) bean;
            } else {
                throw new RuntimeException("注册名称重复 " + name);
            }
        }
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanFactory.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }


    /**
     * 主动向Spring容器中提供单例对象.
     *
     * @param applicationContext Spring容器
     * @param obj                单例对象
     */
    public static void registerBean(ConfigurableApplicationContext applicationContext, Object obj) {
        AssertUtil.notNull(obj, "注册对象是空");
        registerBean(applicationContext, obj.getClass().getSimpleName(), obj);
    }

    /**
     * Register bean.
     *
     * @param applicationContext the application context
     * @param name               the name
     * @param obj                the obj
     */
    public static void registerBean(ConfigurableApplicationContext applicationContext, String name, Object obj) {
        AssertUtil.notEmpty(name, String.format("注册名称是空"));
        AssertUtil.notNull(obj, String.format("注册对象是空"));
        if (applicationContext.containsBean(name)) {
            Object bean = applicationContext.getBean(name);
            AssertUtil.isTrue(bean.getClass().isAssignableFrom(obj.getClass()), String.format("注册名称重复: %s", name));
        }
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        beanFactory.registerSingleton(name, obj);
    }
}
