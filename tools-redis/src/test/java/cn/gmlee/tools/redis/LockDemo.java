package cn.gmlee.tools.redis;

import cn.gmlee.tools.redis.util.RedisLock;

/**
 * RedisLock示例代码
 *
 * @author Jas°
 * @date 2020/12/23 (周三)
 */
public class LockDemo {
    public static final String prefix = "REDIS:LOCK:POS";


    public static void main(String[] args) {
        RedisLock lock = null;
        lock.lock(prefix.concat("business_identity"), 300, () -> {
            System.out.println("这里写需要执行的代码");
            System.out.println("300毫秒没有执行完: 终止执行");
            System.out.println("运行时宕机: 300毫秒后解锁");
        });
        System.out.println();
    }
}
