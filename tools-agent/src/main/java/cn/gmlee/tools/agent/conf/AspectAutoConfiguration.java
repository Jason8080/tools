package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.aop.DynamicAroundAspectBeanPostProcessor;
import cn.gmlee.tools.spring.config.SpringAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
@AutoConfigureAfter({
        SpringAutoConfiguration.class,
})
public class AspectAutoConfiguration {

    private static final String MODE = "AOP";

    private MonitorMethodProperties monitorMethodProperties;

    public AspectAutoConfiguration(@Autowired(required = false) MonitorMethodProperties monitorMethodProperties) {
        if(monitorMethodProperties != null && MODE.equalsIgnoreCase(monitorMethodProperties.getMode())){
            log.info("[Tools AroundAspect] Timing Agent is initializing...");
            this.monitorMethodProperties = monitorMethodProperties;
            log.info("[Tools AroundAspect] Timing Agent is initialized.");
        }
    }

    @Bean
    public DynamicAroundAspectBeanPostProcessor aroundAspect() {
        return new DynamicAroundAspectBeanPostProcessor(monitorMethodProperties);
    }

}
