package cn.gmlee.tools.base.util;

import lombok.extern.slf4j.Slf4j;

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
     * 打印机 (分组统计).
     *
     * @param groups 统计组(2次调用名称需要保持一致)
     * @return long 耗时统计
     */
    public static long printer(String... groups) {
        String state = state(groups);
        long timer = timer(groups);
        String msg = msg(groups);
        log.info("\r\n---------- 计时{} ----------\r\n{}:\t{}/ms\r\n-------------------------------", state, msg, timer);
        return timer;
    }

    /**
     * 打印.
     *
     * @param words 关键字
     * @return long 耗时统计
     */
    public static long println(String... words) {
        String state = state("default");
        long timer = timer("default");
        String msg = msg(words);
        log.info("\r\n---------- 计时{} ----------\r\n{}:\t{}/ms\r\n-------------------------------", state, msg, timer);
        return timer;
    }
}
