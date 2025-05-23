package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.kit.task.TimerTask;
import cn.gmlee.tools.base.kit.task.TimerTaskManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计时器工具
 * <p>
 * 安全提示: 必须关闭,否则会内存泄漏
 * </p>
 */
@Slf4j
public class TimerUtil {

    private final static Map<String, Long> map = new ConcurrentHashMap<>();
    private final static int period = 3600 * 1000; // 1小时毫秒数

    static {
        TimerTaskManager.run(new TimerTask(1000, period){
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                Set<String> groups = map.keySet();
                for (String group : groups) {
                    Long millis = NullUtil.get(map.get(group), 0L);
                    if (current - millis > 24 * TimerUtil.period) {
                        map.remove(group); // 清理1天以上的计数数据
                    }
                }
            }
        });
    }

    private static String group(String... groups) {
        if (BoolUtil.isEmpty(groups)) {
            return Thread.currentThread().getName();
        }
        return Thread.currentThread().getName() + "#" + String.join(" • ", groups);
    }

    private static Long get(String... groups) {
        String group = group(groups);
        return map.get(group);
    }

    private static void set(Long millis, String... groups) {
        String group = group(groups);
        map.put(group, millis);
    }

    private static String msg(String... groups) {
        if (BoolUtil.isEmpty(groups)) {
            return get(groups) != null ? "耗时" : "校准";
        }
        return String.join(" • ", groups);
    }

    private static String state(String... groups) {
        Long last = get(groups);
        if (last != null) {
            return "结束";
        }
        return "开始";
    }

    private static long timer(String... groups) {
        Long last = get(groups); // 先获取
        set(System.currentTimeMillis(), groups); // 再设置
        long millis = System.currentTimeMillis();
        if (last != null) {
            map.remove(group(groups));
            return millis - last;
        }
        return get(groups) - millis; // 起到校准作用
    }

    /**
     * 格式化打印 (分组).
     *
     * @param groups 统计组(2次调用名称需要保持一致)
     * @return long 耗时统计
     */
    public static long info(String... groups) {
        return log(state(groups), msg(groups), timer(groups));
    }

    /**
     * 格式化打印.
     *
     * @param words 关键字
     */
    public static void print(String... words) {
        log(state("default"), msg(words), timer("default"));
    }

    private static long log(String state, String msg, long timer) {
        log.info("\r\n---------- 计时{} ----------\r\n{}:\t{}/ms\r\n-------------------------------", state, msg, timer);
        return timer;
    }
}
