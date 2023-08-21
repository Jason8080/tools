package cn.gmlee.tools.base;

import cn.gmlee.tools.base.alg.bucket.Bucket;
import cn.gmlee.tools.base.alg.bucket.TokenBucket;
import cn.gmlee.tools.base.kit.task.TimerTaskManager;

import java.io.Serializable;

/**
 * 令牌桶算法测试.
 */
public class TokenBucketTests {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // 每秒生成1个
        Bucket bucket = TokenBucket.generate(10, 10000, 10);
        Serializable key1 = TimerTaskManager.submit(() -> TokenBucket.consume(bucket));
        Serializable key2 = TimerTaskManager.submit(() -> TokenBucket.consume(bucket));
        Serializable key3 = TimerTaskManager.submit(() -> TokenBucket.consume(bucket));
        // 每秒消耗1个
        TimerTaskManager.start(key1, 0, 1000);
        // 每2秒被拒绝+1
        TimerTaskManager.start(key2, 0, 2000);
        // 每5秒被拒绝+1
        TimerTaskManager.start(key3, 0, 500);
    }
}
