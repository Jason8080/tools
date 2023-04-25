package cn.gmlee.tools.base.util;

/**
 * 长度控制工具.
 * <p>
 * 长度有余则截取
 * 长度不足则补全
 * </p>
 *
 * @author Jas
 */
public class NumUtil {
    /**
     * 位移运算.
     * <p>
     * offset为0时不做处理.
     * </p>
     *
     * @param num    数值
     * @param offset 位移次数 (正数为左移次数: 变大, 负数为右移次数: 变小)
     * @return 位移后的结果
     */
    public static long offset(long num, int offset) {
        return offset > 0 ? num << offset : num >> offset;
    }
}
