package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.conf.MonitorProperties;
import cn.gmlee.tools.base.anno.Monitor;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class ByteBuddyEnhancer implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    private final MonitorProperties monitorProperties;

    @Override
    public void afterSingletonsInstantiated() {
        if(!BoolUtil.isTrue(monitorProperties.getEnable())){
            return;
        }
        ByteBuddyAgent.install();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String name : beanNames) {
            Object bean = applicationContext.getBean(name);
            Class<?> clazz = bean.getClass();
            // 匹配模式+注解模式
            matching(clazz, UrlUtil.matchOne(monitorProperties.getPackages(), clazz.getPackage().getName()));
        }
    }

    private void matching(Class<?> clazz, boolean match) {
        if (match) {
            enhanceClass(clazz);
        } else if (clazz.isAnnotationPresent(Monitor.class)) {
            enhanceClass(clazz);
        } else {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Monitor.class)) {
                    enhanceClass(clazz);
                    break;
                }
            }
        }
    }

    private void enhanceClass(Class<?> clazz) {
        try {
            new ByteBuddy().redefine(clazz).method(ElementMatchers.any()) // 监控所有方法
                    .intercept(MethodDelegation.to(PassiveTimeoutInterceptor.class)).make()
                    .load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        } catch (Throwable throwable) {
            log.error("[ByteBuddy] 无法增强类：{}", clazz.getName(), throwable);
        }
    }
}
