package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.anno.Monitor;
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
            for (Watcher watcher : MethodMonitorRegistry.all()) {
                long elapsed = watcher.elapsedMillis();
                Monitor annotation = watcher.getMethod().getAnnotation(Monitor.class);
                long timeout = annotation != null ? annotation.timeout() : props.getTimout();
                if (elapsed > timeout) {
                    log.error("\r\n-------------------- Tools Watcher --------------------\r\n[{}] ({}/{}ms)\r\n{}#{}({})",
                            watcher.getThread().getName(),
                            watcher.elapsedMillis(), timeout,
                            watcher.getObj().getClass().getName(),
                            watcher.getMethod().getName(),
                            Arrays.toString(watcher.getArgs())
                    );
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
