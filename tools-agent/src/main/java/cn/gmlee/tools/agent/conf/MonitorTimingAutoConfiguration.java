package cn.gmlee.tools.agent.conf;

import cn.gmlee.tools.agent.listener.MonitorMethodPropertiesChangeApplicationListener;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.AgentTrigger;
import cn.gmlee.tools.agent.trigger.TimeoutTrigger;
import cn.gmlee.tools.agent.watcher.TimeoutWatcher;
import cn.gmlee.tools.base.util.NullUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 方法超时监控自动装配.
 */
@Slf4j
public class MonitorTimingAutoConfiguration {

    private MonitorMethodProperties monitorMethodProperties;

    public MonitorTimingAutoConfiguration(@Autowired(required = false) MonitorMethodProperties props){
        this.monitorMethodProperties = props;
    }

    @Bean
    @ConditionalOnMissingBean(MonitorMethodPropertiesChangeApplicationListener.class)
    public MonitorMethodPropertiesChangeApplicationListener clear(){
        return new MonitorMethodPropertiesChangeApplicationListener();
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
    @ConditionalOnMissingBean(AgentTrigger.class)
    public AgentTrigger byteBuddyTrigger() {
        return new AgentTrigger() {
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
            
        };
    }
}
