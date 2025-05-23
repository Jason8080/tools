package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.kit.thread.AutoCleanThreadLocal;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 计时器工具
 * <p>
 * 安全提示: 必须关闭,否则会内存泄漏
 * </p>
 */
@Slf4j
public class TimerUtil {

    private static ThreadLocal<Map<String, Long>> last = new AutoCleanThreadLocal<>();

    private static String group(String... tips) {
        if (BoolUtil.isEmpty(tips)) {
            return "default";
        }
        return String.join(" • ", tips);
    }

    private static Long get(String... tips) {
        String group = group(tips);
        Map<String, Long> map = last.get();
        return map != null ? map.get(group) : null;
    }

    private static void set(Long millis, String... tips) {
        String group = group(tips);
        Map<String, Long> map = last.get();
        if (map == null) {
            map = new HashMap<>();
            last.set(map);
        }
        map.put(group, millis);
    }

    private static String msg(String... tips) {
        if (BoolUtil.isEmpty(tips)) {
            return get(tips) != null ? "耗时" : "校准";
        }
        return String.join(" • ", tips);
    }


    /**
     * 计时器.
     *
     * @return the long
     */
    public static long timer(String... tips) {
        long millis = System.currentTimeMillis();
        long ms = NullUtil.get(get(tips), millis);
        set(System.currentTimeMillis(), tips);
        return millis - ms;
    }

    /**
     * 打印机.
     *
     * @param tips the tips
     * @return the long
     */
    public static long print(String... tips) {
        long timer = timer(tips);
        log.info("\r\n---------- 计时器提醒 ----------\r\n{}:\t{}/ms\r\n-------------------------------", msg(tips), timer);
        return timer;
    }
}
