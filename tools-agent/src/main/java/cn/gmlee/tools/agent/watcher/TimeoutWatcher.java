package cn.gmlee.tools.agent.watcher;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry;
import cn.gmlee.tools.agent.bytebuddy.ByteBuddyTrigger;
import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.anno.Monitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class TimeoutWatcher {

    private final ApplicationContext ctx;

    private final MonitorMethodProperties props;

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        executor.scheduleAtFixedRate(() -> {
            for (Watcher watcher : ByteBuddyRegistry.all()) {
                long elapsed = watcher.elapsedMillis();
                Monitor monitor = watcher.getOriginalMethod().getAnnotation(Monitor.class);
                long timout = monitor != null ? monitor.timeout() : props.getTimout();
                if (elapsed > timout) {
                    TriggerAssist.trigger(watcher, ByteBuddyTrigger::timout);
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
