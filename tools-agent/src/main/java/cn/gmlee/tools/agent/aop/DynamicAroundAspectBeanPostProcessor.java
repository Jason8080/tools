package cn.gmlee.tools.agent.aop;

import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class DynamicAroundAspectBeanPostProcessor implements BeanPostProcessor {

    private final MonitorMethodProperties props;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ProxyFactory factory = new ProxyFactory(bean);
        factory.addAdvice((MethodInterceptor) invocation -> {
            Object obj = invocation.getThis();
            Method method = invocation.getMethod();
            Object[] args = invocation.getArguments();
            return AroundAspect.getObject(obj, method, args, invocation::proceed);
        });

        return factory.getProxy();
    }
}
