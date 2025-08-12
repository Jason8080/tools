package cn.gmlee.tools.agent.watcher;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry;
import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.agent.trigger.TimeoutTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class TimeoutWatcher {

    private final MonitorMethodProperties props;

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        executor.scheduleAtFixedRate(() -> {
            for (Watcher watcher : ByteBuddyRegistry.all()) {
                TriggerAssist.timout(watcher, TimeoutTrigger::handler);
            }
        }, props.getInitialDelay(), props.getPeriod(), TimeUnit.MILLISECONDS);
    }
}
