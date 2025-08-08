package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.agent.mod.Monitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Arrays;
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
            for (Monitor clock : MethodMonitorRegistry.all()) {
                long elapsed = clock.elapsedMillis();
                cn.gmlee.tools.base.anno.Monitor annotation = clock.getMethod().getAnnotation(cn.gmlee.tools.base.anno.Monitor.class);
                long timeout = annotation != null ? annotation.timeout() : 3000;
                if (elapsed > timeout) {
                    log.error("【Tools Watcher】[{}] ({}ms)\r\n{}#{}({})",
                            clock.getThread().getName(),
                            clock.elapsedMillis(),
                            clock.getObj().getClass().getName(),
                            clock.getMethod().getName(),
                            Arrays.toString(clock.getArgs())
                    );
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
