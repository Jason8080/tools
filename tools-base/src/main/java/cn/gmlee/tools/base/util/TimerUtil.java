package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.kit.task.TimerTask;
import cn.gmlee.tools.base.kit.task.TimerTaskManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
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
                Iterator<Map.Entry<String, Long>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    Long millis = entry.getValue(); // 直接获取值，避免二次查询
                    if (millis == null || current - millis > 24 * TimerUtil.period) {
                        it.remove(); // 使用 Iterator.remove() 原子删除
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
        long current = System.currentTimeMillis();
        Long last = map.putIfAbsent(group(groups), current);
        if (last != null) {
            map.remove(group(groups));
            return current - last;
        }
        return get(groups) - current; // 起到校准作用
    }

    /**
     * 格式化打印 (分组).
     * <p>①关键字有关联: 第1次和第2次必须完全一致</p>
     * <p>①支持嵌套打印: 可以在方法内外部同时使用</p>
     *
     * @param groups 统计组(2次调用名称需要保持一致)
     * @return long 耗时统计
     */
    public static long printf(String... groups) {
        return log(state(groups), msg(groups), timer(groups));
    }

    /**
     * 格式化打印.
     * <p>①关键字没关联: 可以随意抒写</p>
     * <p>①只能串行打印: 不能嵌套打印</p>
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
