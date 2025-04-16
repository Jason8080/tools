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
     * 打印机.
     *
     * @param tips the tips
     * @return the long
     */
    public static long print(String... tips) {
        tips = BoolUtil.isEmpty(tips) ? new String[]{last.get() != null ? "耗时" : "校准"} : tips;
        long timer = timer();
        log.info("\r\n---------- 计时器提醒 ----------\r\n{}:\t{}/ms\r\n-------------------------------", String.join(" • ", tips), timer);
        return timer;
    }


    /**
     * 计时器.
     *
     * @return the long
     */
    public static long timer() {
        long millis = System.currentTimeMillis();
        long ms = NullUtil.get(last.get(), millis);
        last.set(System.currentTimeMillis());
        return millis - ms;
    }
}
