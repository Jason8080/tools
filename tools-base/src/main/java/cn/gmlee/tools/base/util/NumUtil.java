package cn.gmlee.tools.base.util;

import java.util.Arrays;
import java.util.Objects;

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
     * @return 位移后的结果 long
     */
    public static long offset(long num, int offset) {
        return offset > 0 ? num << offset : num >> offset;
    }

    /**
     * Sum long.
     *
     * @param nums the nums
     * @return the long
     */
    public static long sum(long... nums) {
        if(nums == null){
            return 0;
        }
        return Arrays.stream(nums).filter(Objects::nonNull).sum();
    }

    /**
     * Sum int.
     *
     * @param nums the nums
     * @return the int
     */
    public static int sum(int... nums) {
        if(nums == null){
            return 0;
        }
        return Arrays.stream(nums).filter(Objects::nonNull).sum();
    }

    /**
     * Sum double.
     *
     * @param nums the nums
     * @return the double
     */
    public static double sum(double... nums) {
        if(nums == null){
            return 0;
        }
        return Arrays.stream(nums).filter(Objects::nonNull).sum();
    }
}
