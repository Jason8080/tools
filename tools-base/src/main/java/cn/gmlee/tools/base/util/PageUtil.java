package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/**
 * 分页工具.
 */
@Slf4j
public class PageUtil {
    /**
     * 自动翻页.
     *
     * @param <R>  IPage mybatisPlus中的分页对象接口
     * @param page 单页处理程序
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
        while (!BoolUtil.allEmpty(list, data, records)) {
            // 执行处理程序
            log.info("准备处理第{}页结果: {}", current, NullUtil.first(list, data, records));
            ExceptionUtil.suppress(() -> handler.run(NullUtil.first(list, data, records)));
            // 自动翻页程序
            ClassUtil.setValue(r, "current", current + 1);
            ExceptionUtil.suppress(() -> handler.run(NullUtil.first(list, data, records)));
        }
    }
}
