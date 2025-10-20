package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.aop.AspectInitializer;
import cn.gmlee.tools.spring.config.SpringAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
@AutoConfigureAfter({
        SpringAutoConfiguration.class,
})
@Import(AspectInitializer.class)
public class AspectAutoConfiguration {
}
