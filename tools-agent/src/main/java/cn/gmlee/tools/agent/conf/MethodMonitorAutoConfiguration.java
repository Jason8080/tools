package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.bytebuddy.ByteBuddyEnhancer;
import cn.gmlee.tools.agent.bytebuddy.TimeoutWatcher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MonitorProperties.class)
public class MethodMonitorAutoConfiguration {

    @Bean
    public TimeoutWatcher timeoutWatcher(ApplicationContext ctx, MonitorProperties props) {
        return new TimeoutWatcher(ctx, props); // 后台扫描
    }

    @Bean
    public ByteBuddyEnhancer byteBuddyEnhancer(ApplicationContext ctx, MonitorProperties props) {
        return new ByteBuddyEnhancer(ctx, props); // 启动时增强字节码
    }
}
