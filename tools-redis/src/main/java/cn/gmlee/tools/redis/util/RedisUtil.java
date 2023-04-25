package cn.gmlee.tools.redis.util;

/**
 * Redis通用工具 (必须使用SpringBean有效)
 *
 * @author Jas°
 * @date 2020/9/4 (周五)
 */
public class RedisUtil {

    private RedisClient redisClient;
    private RedisLock redisLock;
    private RedisId redisId;

    public RedisUtil(RedisClient redisClient, RedisLock redisLock, RedisId redisId) {
        this.redisClient = redisClient;
        this.redisLock = redisLock;
        this.redisId = redisId;
    }

    public <K, V> RedisClient<K, V> rc(Class<K> kClass, Class<V> vClass) {
        return redisClient;
    }

    public <K, V> RedisClient<K, V> rc() {
        return redisClient;
    }

    public RedisLock rl() {
        return redisLock;
    }

    public RedisId ri() {
        return redisId;
    }
}
