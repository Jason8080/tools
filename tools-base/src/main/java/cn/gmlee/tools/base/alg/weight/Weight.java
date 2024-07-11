package cn.gmlee.tools.base.alg.weight;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 权重算法.
 */
public class Weight {
    public final int allowedPercentage;
    private final AtomicLong counter;
    private final Random random;

    /**
     * Instantiates a new Weight.
     *
     * @param allowedPercentage the allowed percentage
     */
    public Weight(int allowedPercentage) {
        if (allowedPercentage < 0 || allowedPercentage > 100) {
            throw new IllegalArgumentException("Weight allowed percentage must be between 0 and 100");
        }
        this.allowedPercentage = allowedPercentage;
        this.counter = new AtomicLong(0);
        this.random = new Random();
    }

    /**
     * Should allow request boolean.
     *
     * @return the boolean
     */
    public boolean shouldAllowRequest() {
        long currentCount = counter.incrementAndGet();

        if (currentCount % 100 == 0) {
            // 每100次请求重置计数器,避免长时间运行导致的溢出
            counter.set(0);
        }

        return random.nextInt(100) < allowedPercentage;
    }

    public static void main(String[] args) {
        Weight weight = new Weight(98);
        for (int i = 0; i < 100; i++){
            System.out.println(weight.shouldAllowRequest()? "-------------------" : "×××××××××××××××××××××××");
        }
    }
}
