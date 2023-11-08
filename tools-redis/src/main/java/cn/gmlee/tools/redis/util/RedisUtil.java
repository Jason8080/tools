package cn.gmlee.tools.redis.util;

import lombok.RequiredArgsConstructor;

/**
 * Redis通用工具 (必须使用SpringBean有效)
 *
 * @author Jas°
 * @date 2020/9/4 (周五)
 */
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisClient redisClient;
    private final RedisLock redisLock;
    private final RedisId redisId;

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
