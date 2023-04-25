package cn.gmlee.tools.redis.util;

import cn.gmlee.tools.base.util.BoolUtil;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 工具类
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class RedisClient<K, V> {
    private RedisTemplate<K, V> redisTemplate;

    /**
     * Instantiates a new Redis client.
     *
     * @param redisTemplate the redis template
     */
    public RedisClient(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * Keys set.
     *
     * @param key the key
     * @return the set
     */
    public Set<K> keys(K key) {
        return redisTemplate.keys(key);
    }

    /**
     * 设置键值.
     *
     * @param key the key
     * @param val the val
     */
    public void set(K key, V val) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        ops.set(key, val);
    }

    /**
     * 在前面插入1个值.
     *
     * @param key the key
     * @param val the val
     */
    public void listInsert(K key, V val) {
        ListOperations<K, V> ops = redisTemplate.opsForList();
        ops.leftPush(key, val);
    }

    /**
     * 在后面追加1个值.
     *
     * @param key the key
     * @param val the val
     */
    public void listAdd(K key, V val) {
        ListOperations<K, V> ops = redisTemplate.opsForList();
        ops.rightPush(key, val);
    }

    /**
     * 获取全部内容.
     *
     * @param key the key
     * @return list list
     */
    public List<V> list(K key) {
        ListOperations<K, V> ops = redisTemplate.opsForList();
        return ops.range(key, 0, -1);
    }


    /**
     * 是否包含目标值.
     *
     * @param key the key
     * @param val the val
     * @return the boolean
     */
    public boolean contain(K key, V val) {
        List<V> list = list(key);
        return BoolUtil.containOne(list, val);
    }


    /**
     * 设置键值，当且仅当 key 不存在才设置.
     *
     * @param key the key
     * @param val the val
     * @return nx nx
     */
    public Boolean setNx(K key, V val) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.setIfAbsent(key, val);
    }

    /**
     * 设置新值，当且仅当 key 存在且值不相同才设置.
     *
     * @param key the key
     * @param val the val
     * @return ex ex
     */
    public Boolean setEx(K key, V val) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.setIfPresent(key, val);
    }

    /**
     * 设置键值.
     *
     * @param key    the key
     * @param val    the val
     * @param expire the expire second
     */
    public void set(K key, V val, long expire) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        ops.set(key, val, expire, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置键值，当且仅当 key 不存在才设置.
     *
     * @param key    the key
     * @param val    the val
     * @param expire the expire second
     * @return nx nx
     */
    public Boolean setNx(K key, V val, long expire) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.setIfAbsent(key, val, expire, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置新值，当且仅当 key 存在才设置.
     *
     * @param key    the key
     * @param val    the val
     * @param expire the expire second
     * @return ex ex
     */
    public Boolean setEx(K key, V val, long expire) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.setIfPresent(key, val, expire, TimeUnit.MILLISECONDS);
    }

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * 获取值.
     *
     * @param key the key
     * @return the object
     */
    public V get(K key) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    /**
     * 获取过期时间 (秒).
     *
     * @param key the key
     * @return expire 单位秒
     */
    public Long getExpire(K key) {
        return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    }

    /**
     * 递增／递减.
     *
     * @param key   the key
     * @param delta the delta
     * @return long long
     */
    public Long increment(K key, Long delta) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.increment(key, delta);
    }

    /**
     * 递增／递减.
     *
     * @param key   the key
     * @param delta the delta
     * @return double double
     */
    public Double increment(K key, Double delta) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.increment(key, delta);
    }

    /**
     * 设置新值并返回旧值.
     *
     * @param key the key
     * @param val the val
     * @return the v
     */
    public V getAndSet(K key, V val) {
        ValueOperations<K, V> ops = redisTemplate.opsForValue();
        return ops.getAndSet(key, val);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 判断是否存在.
     *
     * @param key the key
     * @return the boolean
     */
    public Boolean exists(K key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除指定键.
     *
     * @param key the key
     * @return the boolean
     */
    public Boolean delete(K key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除指定键.
     *
     * @param keys the keys
     * @return the long
     */
    public Long delete(Collection<K> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 添加过期时间 (毫秒).
     *
     * @param key    the key
     * @param expire the expire second
     * @return the expire
     */
    public Boolean addExpire(K key, long expire) {
        return redisTemplate.expire(key, expire, TimeUnit.MILLISECONDS);
    }
}
