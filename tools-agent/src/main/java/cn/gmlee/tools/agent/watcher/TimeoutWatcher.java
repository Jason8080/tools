package cn.gmlee.tools.agent.watcher;

import cn.gmlee.tools.agent.assist.TriggerAssist;
import cn.gmlee.tools.agent.bytebuddy.ByteBuddyRegistry;
import cn.gmlee.tools.agent.conf.MonitorMethodProperties;
import cn.gmlee.tools.agent.mod.Watcher;
import cn.gmlee.tools.base.util.BoolUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.*;
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
        executor.scheduleAtFixedRate(this::actuator, props.getInitialDelay(), props.getPeriod(), TimeUnit.MILLISECONDS);
    }

    private void actuator() {
        try {
            Map<Thread, Set<Watcher>> all = new HashMap<>(ByteBuddyRegistry.all());
            // 线程复用清理
            all.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().isInterrupted());
            Iterator<Map.Entry<Thread, Set<Watcher>>> it = all.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<Thread, Set<Watcher>> next = it.next();
                Thread thread = next.getKey();
                Set<Watcher> set = next.getValue();
                LinkedHashSet<Watcher> watchers = new LinkedHashSet<>(set);
                if (BoolUtil.isEmpty(watchers)) {
                    it.remove();
                    continue;
                }
                // 最大存活时间
                Watcher watcher = watchers.stream()
                        .max(Comparator.comparing(Watcher::elapsedMillis))
                        .orElse(null);
                if (watcher != null && watcher.elapsedMillis() > props.getMaxSurvival()) {
                    it.remove();
                    continue;
                }
                // 触发超时监控
                TriggerAssist.timout(thread, watcher, watchers);
            }
        } catch (Throwable throwable) {
            log.error("超时监控定时器执行异常", throwable);
        }
    }
}
