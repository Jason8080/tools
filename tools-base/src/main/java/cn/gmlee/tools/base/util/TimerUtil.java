package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.kit.thread.AutoCleanThreadLocal;
import lombok.extern.slf4j.Slf4j;

/**
 * 计时器工具
 * <p>
 * 安全提示: 必须关闭,否则会内存泄漏
 * </p>
 */
@Slf4j
public class TimerUtil {

    private static ThreadLocal<Long> last = new AutoCleanThreadLocal<>();

    /**
     * Print.
     *
     * @param tips the tips
     */
    public static void print(String... tips) {
        tips = BoolUtil.isEmpty(tips) ? new String[]{last.get() != null ? "耗时" : "校准"} : tips;
        long millis = System.currentTimeMillis();
        Long ms = NullUtil.get(last.get(), millis);
        log.info("\r\n---------- 计时器提醒 ----------\r\n{}:\t{}/ms\r\n-------------------------------", String.join(" • ", tips), millis - ms);
        last.set(System.currentTimeMillis());
    }
}
