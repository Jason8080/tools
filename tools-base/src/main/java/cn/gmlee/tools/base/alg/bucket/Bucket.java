package cn.gmlee.tools.base.alg.bucket;

import cn.gmlee.tools.base.util.BigDecimalUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 桶.
 */
@Data
@Slf4j
public class Bucket implements Serializable {
    /**
     * 当前令牌数
     */
    private final AtomicInteger num = new AtomicInteger(0);
    /**
     * 下次生成令牌的时间戳
     */
    private long timestamp = System.currentTimeMillis();
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
     * 生成1个令牌所需的时间
     */
    private BigDecimal one;


    /**
     * 初始化令牌桶.
     * <p>
     * 默认: 每秒钟生成{@param capacity}个令牌
     * </p>
     *
     * @param capacity the capacity
     */
    public Bucket(int capacity) {
        this(capacity, capacity);
    }

    /**
     * 初始化令牌桶.
     * <p>
     * 默认每秒钟生成{@param qty}个令牌
     * </p>
     *
     * @param capacity the capacity
     * @param qty      the qty
     */
    public Bucket(int capacity, int qty) {
        this(capacity, 1000, qty);
    }

    /**
     * 初始化令牌桶.
     * <p>
     * 默认单次消耗1个令牌
     * </p>
     *
     * @param capacity the capacity
     * @param time     the time
     * @param qty      the qty
     */
    public Bucket(int capacity, int time, int qty) {
        this(capacity, 1, time, qty);
    }

    /**
     * 初始化令牌桶.
     *
     * @param capacity 容量(最大令牌数)
     * @param caliber  口径(单次消耗令牌的数量)
     * @param time     时间(生成的{@param qty}令牌数需要的时间)
     * @param qty      数量({@param time}时间内生成的令牌数量)
     */
    public Bucket(int capacity, int caliber, int time, int qty) {
        this.capacity = capacity;
        this.caliber = caliber;
        this.time = time;
        this.one = BigDecimalUtil.divide(time, qty);
        // 初始化最大值
        this.num.set(capacity);
    }

    /**
     * 重置令牌数
     * <p>添加令牌/销毁令牌</p>
     *
     * @param num 正数: 添加; 负数: 销毁
     * @return 更新后数量 int
     */
    public boolean recharge(int num) {
        boolean allow = this.num.get() + num >= 0;
        if (allow) {
            this.num.addAndGet(num);
        }
        return allow;
    }
}
