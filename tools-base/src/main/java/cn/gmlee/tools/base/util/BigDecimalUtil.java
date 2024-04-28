package cn.gmlee.tools.base.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 浮点数工具
 *
 * @author Jas °
 * @date 2021 /7/6 (周二)
 */
public class BigDecimalUtil {
    public static final int SCALE_2 = 2; // 金额
    public static final int SCALE_3 = 3; // 吨数
    public static final int SCALE_4 = 4; // 比例
    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100); // 100

    /**
     * Get big decimal.
     *
     * @param obj the obj
     * @return the big decimal
     */
    public static BigDecimal get(Object obj) {
        if (obj != null) {
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            } else if (obj instanceof Double) {
                return BigDecimal.valueOf((Double) obj);
            } else if (obj instanceof Integer) {
                return new BigDecimal((Integer) obj);
            } else if (obj instanceof Long) {
                return BigDecimal.valueOf((Long) obj);
            } else if (obj instanceof String) {
                return new BigDecimal((String) obj);
            } else if (obj instanceof BigInteger) {
                return new BigDecimal((BigInteger) obj);
            } else {
                ExceptionUtil.cast(String.format("不支持的类型: %s", obj.getClass()));
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 加法.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal add(BigDecimal num, BigDecimal... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            for (BigDecimal n : nums) {
                num = num.add(n);
            }
        }
        return NullUtil.get(num, BigDecimal.ZERO);
    }

    /**
     * 减法.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal subtract(BigDecimal num, BigDecimal... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            for (BigDecimal n : nums) {
                num = num.subtract(n);
            }
        }
        return NullUtil.get(num, BigDecimal.ZERO);
    }

    /**
     * 乘法 .
     *
     * @param num         the num
     * @param multipliers the multipliers
     * @return the big decimal
     */
    public static BigDecimal multiply(BigDecimal num, BigDecimal... multipliers) {
        if (BoolUtil.allNotNull(num, multipliers)) {
            for (BigDecimal multiplier : multipliers) {
                if (BoolUtil.eq(multiplier, BigDecimal.ZERO)) {
                    return BigDecimal.ZERO;
                }
                num = num.multiply(multiplier)/*.setScale(SCALE_2, BigDecimal.ROUND_HALF_UP)*/;
            }
        }
        return NullUtil.get(num, BigDecimal.ZERO);
    }

    /**
     * 除法 .
     *
     * @param num      the num
     * @param divisors divisors
     * @return the big decimal
     */
    public static BigDecimal divide(BigDecimal num, BigDecimal... divisors) {
        if (BoolUtil.allNotNull(num, divisors)) {
            for (BigDecimal divisor : divisors) {
                if (BoolUtil.eq(divisor, BigDecimal.ZERO)) {
                    return BigDecimal.ZERO;
                }
                num = num.divide(divisor)/*.setScale(SCALE_2, BigDecimal.ROUND_HALF_UP)*/;
            }
        }
        return NullUtil.get(num, BigDecimal.ZERO);
    }

    /**
     * 元转分: x 100.
     *
     * @param yuan the yuan
     * @return the big decimal
     */
    public static BigDecimal yuan2fen(BigDecimal yuan) {
        if (BoolUtil.notNull(yuan)) {
            return multiply(yuan, ONE_HUNDRED);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 分转元: ÷ 100.
     *
     * @param fen the fen
     * @param rm  the rm
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(BigDecimal fen, RoundingMode rm) {
        if (BoolUtil.notNull(fen)) {
            return fen.divide(ONE_HUNDRED, SCALE_2, rm);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 分转元: 多余的小数直接抹除.
     *
     * @param fen the fen
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(BigDecimal fen) {
        if (BoolUtil.notNull(fen)) {
            return fen.divide(ONE_HUNDRED, SCALE_2, RoundingMode.DOWN);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 由于该方法的参数类型太广泛不适用所有场景
     * 所以不要开放共用
     *
     * @param nums
     * @return
     */
    private static BigDecimal[] convert(Object... nums) {
        if (BoolUtil.notEmpty(nums)) {
            BigDecimal[] decimals = new BigDecimal[nums.length];
            for (int i = 0; i < decimals.length; i++) {
                decimals[i] = get(nums[i]);
            }
            return decimals;
        }
        return new BigDecimal[0];
    }

    // 泛数字工具有异常 --------------------------------------------------------------------------------------------------

    /**
     * 泛数字运算 (不支持的类型将抛出异常).
     * <p>
     * 仅支持: Double、Integer、Long、BigInteger, String(数字)
     * </p>
     *
     * @param num  the num
     * @param nums the nums
     * @return big decimal
     */
    public static BigDecimal add(Object num, Object... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return add(get(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 泛数字运算 (不支持的类型将抛出异常).
     * <p>
     * 仅支持: Double、Integer、Long、BigInteger, String(数字)
     * </p>
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal subtract(Object num, Object... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return subtract(get(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 泛数字运算 (不支持的类型将抛出异常).
     * <p>
     * 仅支持: Double、Integer、Long、BigInteger, String(数字)
     * </p>
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal multiply(Object num, Object... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return multiply(get(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * 泛数字运算 (不支持的类型将抛出异常).
     * <p>
     * 仅支持: Double、Integer、Long、BigInteger, String(数字)
     * </p>
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal divide(Object num, Object... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return divide(get(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    // 下面都是重载依赖 --------------------------------------------------------------------------------------------------

    /**
     * Add big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal add(Double num, Double... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return add(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Subtract big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal subtract(Double num, Double... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return subtract(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Multiply big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal multiply(Double num, Double... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return multiply(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Divide big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal divide(Double num, Double... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return divide(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Add big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal add(Integer num, Integer... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return add(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Subtract big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal subtract(Integer num, Integer... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return subtract(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Multiply big decimal.
     *
     * @param num  the num 1
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal multiply(Integer num, Integer... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return multiply(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Divide big decimal.
     *
     * @param num  the num 1
     * @param nums the num 2
     * @return the big decimal
     */
    public static BigDecimal divide(Integer num, Integer... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return divide(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }


    /**
     * Add big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal add(Long num, Long... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return add(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Subtract big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal subtract(Long num, Long... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return subtract(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Multiply big decimal.
     *
     * @param num  the num 1
     * @param nums the num 2
     * @return the big decimal
     */
    public static BigDecimal multiply(Long num, Long... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return multiply(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Divide big decimal.
     *
     * @param num  the num 1
     * @param nums the num 2
     * @return the big decimal
     */
    public static BigDecimal divide(Long num, Long... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return divide(BigDecimal.valueOf(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Add big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal add(String num, String... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return add(new BigDecimal(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Subtract big decimal.
     *
     * @param num  the num
     * @param nums the nums
     * @return the big decimal
     */
    public static BigDecimal subtract(String num, String... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return subtract(new BigDecimal(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Multiply big decimal.
     *
     * @param num  the num 1
     * @param nums the num 2
     * @return the big decimal
     */
    public static BigDecimal multiply(String num, String... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return multiply(new BigDecimal(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Divide big decimal.
     *
     * @param num  the num 1
     * @param nums the num 2
     * @return the big decimal
     */
    public static BigDecimal divide(String num, String... nums) {
        if (BoolUtil.allNotNull(num, nums)) {
            return divide(new BigDecimal(num), convert(nums));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Rmb y 2 f big decimal.
     *
     * @param yuan the yuan
     * @return the big decimal
     */
    public static BigDecimal yuan2fen(Double yuan) {
        if (BoolUtil.notNull(yuan)) {
            return multiply(new BigDecimal(yuan), ONE_HUNDRED);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Yuan 2 fen big decimal.
     *
     * @param yuan the yuan
     * @return the big decimal
     */
    public static BigDecimal yuan2fen(Integer yuan) {
        if (BoolUtil.notNull(yuan)) {
            return multiply(new BigDecimal(yuan), ONE_HUNDRED);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Yuan 2 fen big decimal.
     *
     * @param yuan the yuan
     * @return the big decimal
     */
    public static BigDecimal yuan2fen(Long yuan) {
        if (BoolUtil.notNull(yuan)) {
            return multiply(new BigDecimal(yuan), ONE_HUNDRED);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Yuan 2 fen big decimal.
     *
     * @param yuan the yuan
     * @return the big decimal
     */
    public static BigDecimal yuan2fen(String yuan) {
        if (BoolUtil.notNull(yuan)) {
            return multiply(new BigDecimal(yuan), ONE_HUNDRED);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(Double fen) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @param rm  the rm
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(Double fen, RoundingMode rm) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen), rm);
        }
        return BigDecimal.ZERO;
    }


    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(Integer fen) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @param rm  the rm
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(Integer fen, RoundingMode rm) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen), rm);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(Long fen) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @param rm  the rm
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(Long fen, RoundingMode rm) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen), rm);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(String fen) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Fen 2 yuan big decimal.
     *
     * @param fen the fen
     * @param rm  the rm
     * @return the big decimal
     */
    public static BigDecimal fen2yuan(String fen, RoundingMode rm) {
        if (BoolUtil.notNull(fen)) {
            return fen2yuan(new BigDecimal(fen), rm);
        }
        return BigDecimal.ZERO;
    }

}
