package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XTime;
import lombok.extern.slf4j.Slf4j;

/**
 * 计时器工具
 * <p>
 * 安全提示: 必须关闭,否则会内存泄漏
 * </p>
 */
@Slf4j
public class TimerUtil {
    /**
     * 最后一个时间
     */
    private static ThreadLocal<Long> last = new ThreadLocal<>();

    /**
     * 启动计时.
     */
    public static void start() {
        last.set(System.currentTimeMillis());
        log.info("---------- 计时启动 ---------- {}", TimeUtil.getCurrentDatetime(XTime.MS_MINUS_BLANK_COLON_DOT));
    }

    /**
     * Print.
     *
     * @param msg the msg
     */
    public static void print(String msg) {
        Long ms = last.get();
        if (ms == null) {
            log.warn("请先启动计时(start)记得关闭(close)哦");
            return;
        }
        long millis = System.currentTimeMillis();
        log.info("---------- 耗时提醒 ----------\r\n{}:\t{}/ms\r\n-----------------------------", msg, millis - ms);
        last.set(millis);
    }

    /**
     * 关闭计时.
     * <p>
     * 安全提示: 必须关闭,否则会内存泄漏
     * </p>
     */
    public static void close() {
        last.remove();
        log.info("---------- 计时关闭 ---------- {}", TimeUtil.getCurrentDatetime(XTime.MS_MINUS_BLANK_COLON_DOT));
    }
}
