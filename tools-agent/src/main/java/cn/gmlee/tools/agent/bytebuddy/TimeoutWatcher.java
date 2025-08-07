package cn.gmlee.tools.agent.bytebuddy;

import cn.gmlee.tools.agent.conf.MonitorProperties;
import cn.gmlee.tools.agent.mod.MethodClock;
import cn.gmlee.tools.base.anno.Monitor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TimeoutWatcher {

    private final ApplicationContext applicationContext;

    private final MonitorProperties monitorProperties;

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void start() {
        executor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (MethodClock info : MethodMonitorRegistry.all()) {
                long elapsed = now - info.startTime;
                Monitor annotation = info.method.getAnnotation(Monitor.class);
                long timeout = annotation != null ? annotation.timeout() : 3000;
                if (elapsed > timeout) {
                    System.err.printf("[ALERT] 方法 [%s] 在线程 [%s] 执行超时：%d ms (阈值：%d ms)%n",
                            info.method.getName(), info.thread.getName(), elapsed, timeout);
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
