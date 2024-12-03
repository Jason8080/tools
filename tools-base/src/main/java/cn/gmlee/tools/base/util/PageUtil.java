package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import cn.gmlee.tools.base.enums.Int;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 分页工具.
 */
@Slf4j
public class PageUtil {
    /**
     * 自动翻页.
     *
     * @param <R>     IPage mybatisPlus中的分页对象接口
     * @param <P>     the type parameter
     * @param page    单页处理程序
     * @param handler the handler
     */
    public static <R, P extends Collection> void nextPage(Function.Zero2r<R> page, Function.One<P> handler) {
        // 获取 1 页
        R r = ExceptionUtil.suppress(page);
        AssertUtil.notNull(r, "获取分页结果失败");
        Long current = ClassUtil.getValue(r, "current");
        AssertUtil.notNull(current, "获取当前页码失败");
        log.info("获取到第{}页结果: {}", current, r);
        Map<String, Object> map = ClassUtil.generateCurrentMap(r);
        P list = (P) map.get("list");
        P data = (P) map.get("data");
        P records = (P) map.get("records");
        P p = NullUtil.first(list, data, records);
        if (BoolUtil.notEmpty(p)) {
            // 执行处理程序
            log.info("准备处理第{}页结果: {}", current, p);
            ExceptionUtil.sandbox(() -> handler.run(p), () -> log.error("处理第{}页发生异常", current));
            // 自动翻页程序
            ClassUtil.setValue(r, "current", current + 1);
            nextPage(page, handler);
        }
    }

    /**
     * 指定页数分页.
     *
     * @param <T>  the type parameter
     * @param ts   the ts
     * @param page 拆分页数
     * @return list 返回确定的页数
     */
    public static <T> List<List<T>> splitPage(List<T> ts, int page) {
        List<List<T>> pages = new ArrayList<>(page);
        if (BoolUtil.isEmpty(ts) || page <= Int.ONE) {
            pages.add(ts);
            return pages;
        }
        for (int i = 0, j = 0; i < ts.size(); i++, j++) {
            if (j >= page) {
                j = 0;
            }
            if (pages.size() < page) {
                pages.add(new ArrayList<>());
            }
            List<T> list = pages.get(j);
            list.add(ts.get(i));
        }
        return pages;
    }


    /**
     * 指定容量分页.
     *
     * @param <T>  the type parameter
     * @param ts   the ts
     * @param size 每页数量
     * @return list 返回不确定的页数
     */
    public static <T> List<List<T>> splitSize(List<T> ts, int size) {
        List<List<T>> pages = new ArrayList<>();
        if (BoolUtil.isEmpty(ts) || size < Int.ONE) {
            return pages;
        }
        int total = ts.size(); // 计算页数
        int page = (int) Math.ceil(BigDecimalUtil.divide(total, size).doubleValue());
        for (int i = 0, j = 0; i < total; i++, j++) {
            if (j >= page) {
                j = 0;
            }
            if (pages.size() < page) {
                pages.add(new ArrayList<>(size));
            }
            List<T> list = pages.get(j);
            list.add(ts.get(i));
        }
        return pages;
    }
}
