package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.BigDecimalUtil;

/**
 * .
 *
 * @author Jas°
 * @date 2021/7/27 (周二)
 */
public class BigDecimalTests {
    public static void main(String[] args) {
        System.out.println(BigDecimalUtil.add(66, 2, 3, 5));
        System.out.println(BigDecimalUtil.subtract(66, 2, 3, 5));
        System.out.println(BigDecimalUtil.multiply(66, 2, 3, 5));
        System.out.println(BigDecimalUtil.divide(66, 2, 3, 5));
    }
}
