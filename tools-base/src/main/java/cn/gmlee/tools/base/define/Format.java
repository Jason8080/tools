package cn.gmlee.tools.base.define;

/**
 * 数据格式化.
 *
 * @param <I> 输入
 * @param <O> 输出
 */
@FunctionalInterface
public interface Format<I, O> {
    /**
     * 格式化.
     *
     * @param i the
     * @return the o
     */
    O format(I i);
}