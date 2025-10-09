package cn.gmlee.tools.agent.listener;

import cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry;
import cn.gmlee.tools.base.util.QuickUtil;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import java.util.Optional;

public class MonitorMethodPropertiesChangeApplicationListener implements ApplicationListener<EnvironmentChangeEvent> {

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Optional<String> any = event.getKeys().stream().filter(x -> x.contains("monitor")).findAny();
        QuickUtil.isTrue(any.isPresent(), ByteBuddyRegistry::remove);
    }
}