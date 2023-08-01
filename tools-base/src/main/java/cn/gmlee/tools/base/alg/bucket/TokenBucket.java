package cn.gmlee.tools.base.alg.bucket;

import cn.gmlee.tools.base.util.BigDecimalUtil;

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
public class TokenBucket {
    /**
     * 容量(最大令牌数)
     */
    private final int capacity;
    /**
     * 口径(每次消耗令牌数)
     */
    private final int caliber;
    /**
     * 时间(毫秒).
     */
    private final int time;
    /**
     * 数量
     */
    private final int qty;
    /**
     * 生成1个令牌所需的时间
     */
    private BigDecimal one;
    /**
     * 下次生成令牌的时间戳
     */
    private long timestamp;

    /**
     * Instantiates a new Token bucket.
     *
     * @param capacity the capacity
     * @param caliber  the caliber
     * @param time     the time
     * @param qty      the qty
     */
    public TokenBucket(int capacity, int caliber, int time, int qty) {
        this.capacity = capacity;
        this.caliber = caliber;
        this.time = time;
        this.qty = qty;
        this.one = BigDecimalUtil.divide(time, qty);
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取当前时间 到 下次生成时间 可以生成的令牌数.
     */
    public void generate(){
        // 当前时间
        long current = System.currentTimeMillis();
//        long l = BigDecimalUtil.divide(current - timestamp) / one;
    }
}
