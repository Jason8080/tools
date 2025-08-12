package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.ByteBuddyTrigger;
import cn.gmlee.tools.agent.trigger.TimeoutTrigger;
import cn.gmlee.tools.agent.watcher.TimeoutWatcher;
import cn.gmlee.tools.base.util.NullUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonitorTimingAutoConfiguration {

    private MonitorMethodProperties monitorMethodProperties;

    public MonitorTimingAutoConfiguration(@Autowired(required = false) MonitorMethodProperties props){
        this.monitorMethodProperties = props;
    }

    /**
     * 超时监控.
     *
     * @return the timeout watcher
     */
    @Bean
    @ConditionalOnMissingBean(TimeoutWatcher.class)
    public TimeoutWatcher timeoutWatcher() {
        return new TimeoutWatcher(NullUtil.get(monitorMethodProperties, MonitorMethodProperties::new));
    }

    /**
     * Byte buddy trigger byte buddy trigger.
     *
     * @return the byte buddy trigger
     */
    @Bean
    @ConditionalOnMissingBean(ByteBuddyTrigger.class)
    public ByteBuddyTrigger byteBuddyTrigger() {
        return new ByteBuddyTrigger() {
            @Override
            public void enter(Watcher watcher) {

            }

            @Override
            public void exit(Watcher watcher) {

            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(TimeoutTrigger.class)
    public TimeoutTrigger timeoutTrigger() {
        return new TimeoutTrigger() {
            @Override
            public void handle(Watcher watcher, long elapsed, long timout) {
                log.warn("\r\n-------------------- Tools Watcher --------------------\r\n[{}] ({}/{})ms\r\n{}#{}({})",
                        watcher.getThread().getName(),
                        watcher.elapsedMillis(), timout,
                        watcher.getOriginalObj().getClass().getName(),
                        watcher.getOriginalMethod().getName(),
                        Arrays.toString(NullUtil.get(watcher.getOriginalArgs(), watcher.getArgs()))
                );
            }
        };
    }
}
