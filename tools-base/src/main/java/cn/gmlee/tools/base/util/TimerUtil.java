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

    private static String group(String... tips) {
        if (BoolUtil.isEmpty(tips)) {
            return Thread.currentThread().getName();
        }
        return String.join(" • ", tips) + "_" + Thread.currentThread().getId();
    }

    private static Long get(String... tips) {
        String group = group(tips);
        return map.get(group);
    }

    private static void set(Long millis, String... tips) {
        String group = group(tips);
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
        Long last = get(tips);
        long millis = System.currentTimeMillis();
        if (last != null) {
            map.remove(group(tips));
            return millis - last;
        }
        set(System.currentTimeMillis(), tips);
        return 0;
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
