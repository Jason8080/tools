package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.aop.AopAspect;
import cn.gmlee.tools.spring.config.SpringAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
@AutoConfigureBefore({
        SpringAutoConfiguration.class,
        ByteBuddyAutoConfiguration.class,
})
public class AopAutoConfiguration {

    private static final String MODE = "AOP";

    private MonitorMethodProperties monitorMethodProperties;

    public AopAutoConfiguration(@Autowired(required = false) MonitorMethodProperties monitorMethodProperties) {
        if (monitorMethodProperties != null && MODE.equalsIgnoreCase(monitorMethodProperties.getMode())) {
            log.info("[Tools AopAspect] Timing Agent is initializing...");
            this.monitorMethodProperties = monitorMethodProperties;
            log.info("[Tools AopAspect] Timing Agent is initialized.");
        }
    }

    @Bean
    public AopAspect aopAspect() {
        return new AopAspect(monitorMethodProperties);
    }
}
