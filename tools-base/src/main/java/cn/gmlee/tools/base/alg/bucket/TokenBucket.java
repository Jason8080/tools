package cn.gmlee.tools.base.alg.bucket;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.BigDecimalUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 令牌桶算法.
 * <p>
 * 场景: 访问限流, 业务控速
 * ----------------------------
 * 需求说明: 1分钟内最多处理10个请求
 * ----------------------------
 * 常规限流如下(跨阶段限流失败):
 * 时间秒: 58 59 1 2 3 4 5 6 .. 58 59
 * 请求数:    10 10
 * ----------------------------
 * 令牌桶限流如下:
 * 时间秒: 58 59 1 2 3 4 5 6 .. 58 59
 * 请求数:    10 0 0 0 0 0 1
 * </p>
 */
@Slf4j
public class TokenBucket {
    /**
     * 构建桶.
     *
     * @param capacity the capacity
     * @param time     the time
     * @param qty      the qty
     * @return the bucket
     */
    public static Bucket generate(int capacity, int time, int qty) {
        return new Bucket(capacity, time, qty);
    }

    /**
     * 消费.
     *
     * @param bucket the bucket
     * @return the boolean
     */
    public static boolean consume(Bucket bucket) {
        return consume(bucket, bucket.getCaliber());
    }

    /**
     * 消费.
     *
     * @param bucket  the bucket
     * @param caliber 消费令牌数
     * @return boolean 是否允许访问
     */
    public static boolean consume(Bucket bucket, int caliber) {
        // 当前时间
        String current = TimeUtil.getCurrentDatetime(XTime.MS_MINUS_BLANK_COLON_DOT);
        // 生成令牌
        int number = /*bucket.getNum().get() >= caliber  ? 0 : */generate(bucket); // 生成数量
        int post = bucket.getNum().get(); // 生成后令牌数
        // 消耗令牌
        boolean allow = bucket.recharge(-caliber);
        int complete = bucket.getNum().get(); // 消耗后数量
        String symbol = allow ? (number > 0 ? "++++++++++" : "----------"): "××××××××××";
        log.debug(symbol.concat(" 时间: {}; 生成: {}; 当前: {}; 消耗: {}; 剩余: {}; 允许: {}  ").concat(symbol), current, number, post, caliber, complete, allow);
        return allow;
    }

    /**
     * 计算生成令牌数
     *
     * @param bucket
     * @param current
     * @return
     */
    private static int calculate(Bucket bucket, long current) {
        // 可以生成数量
        BigDecimal number = BigDecimalUtil.divide(bucket.getTimestamp() - current, bucket.getOne());
        // 最大生成数量
        int max = bucket.getCapacity() - bucket.getNum().get();
        // 取较小的数值
        return Math.min(max, number.intValue());
    }

    /**
     * 获取当前时间 到 下次生成时间 可以生成的令牌数.
     *
     * @param bucket the bucket
     * @return
     */
    private static int generate(Bucket bucket) {
        // 当前时间
        long current = System.currentTimeMillis();
        // 不予生成
        if (current < bucket.getTimestamp()) {
            return 0;
        }
        // 计算下次生成时间
        bucket.setTimestamp(current + bucket.getTime());
        // 本次生成令牌数量
        int number = calculate(bucket, current);
        // 重置令牌数
        bucket.recharge(number);
        return number;
    }
}
