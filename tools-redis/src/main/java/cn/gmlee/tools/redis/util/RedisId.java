package cn.gmlee.tools.redis.util;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.TimeUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import java.util.Calendar;
import java.util.Date;

/**
 * redis 分布式ID生成器
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class RedisId {
    private RedisTemplate redisTemplate;

    /**
     * Instantiates a new Redis id.
     *
     * @param redisTemplate the redis template
     */
    public RedisId(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取现有ID值.
     *
     * @param key the key
     * @return the long
     */
    public long get(String key) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return counter.get();
    }

    /**
     * 获取现有ID值.
     *
     * @param key the key
     * @return int int
     */
    public int getInt(String key) {
        RedisAtomicInteger counter = new RedisAtomicInteger(key, redisTemplate.getConnectionFactory());
        return counter.get();
    }

    /**
     * 重置ID初始值(并生成下一个ID).
     *
     * @param key the key
     * @param val the val
     * @return the long
     */
    public long reset(String key, long val) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.set(val);
        return counter.incrementAndGet();
    }


    /**
     * 重置ID初始值(并生成下一个ID).
     *
     * @param key the key
     * @param val the val
     * @return int
     */
    public int resetInt(String key, int val) {
        RedisAtomicInteger counter = new RedisAtomicInteger(key, redisTemplate.getConnectionFactory());
        counter.set(val);
        return counter.incrementAndGet();
    }

    /**
     * 生成自增ID
     *
     * @param key the key
     * @return the long
     */
    public long generate(String key) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return counter.incrementAndGet();
    }

    /**
     * Generate int int.
     *
     * @param key the key
     * @return the long
     */
    public int generateInt(String key) {
        RedisAtomicInteger counter = new RedisAtomicInteger(key, redisTemplate.getConnectionFactory());
        return counter.incrementAndGet();
    }

    /**
     * 生成自增ID (自动过期)
     *
     * @param key        the key
     * @param expireTime the expire time
     * @return the long
     */
    public long generate(String key, Date expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expireAt(expireTime);
        return counter.incrementAndGet();
    }

    /**
     * 生成增量ID
     *
     * @param key       the key
     * @param increment the increment
     * @return the long
     */
    public long generate(String key, int increment) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return counter.addAndGet(increment);
    }

    /**
     * Generate int int.
     *
     * @param key       the key
     * @param increment the increment
     * @return the int
     */
    public int generateInt(String key, int increment) {
        RedisAtomicInteger counter = new RedisAtomicInteger(key, redisTemplate.getConnectionFactory());
        return counter.addAndGet(increment);
    }

    /**
     * 生成增量ID (自动过期)
     *
     * @param key        the key
     * @param increment  the increment
     * @param expireTime the expire time
     * @return the long
     */
    public long generate(String key, int increment, Date expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expireAt(expireTime);
        return counter.addAndGet(increment);
    }

    /**
     * 生成当天指定长度的ID (自动过期)
     * <p>
     * length = 20 ,当天日期2020年12月12日
     * 结果: 20201212 00000000000000000001
     * </p>
     *
     * @param key the key
     * @return the string
     */
    public String generateToday(String key) {
        // long类型的最大值就是20位: 相当于没有限制长度
        return generateToday(key, 20);
    }

    /**
     * 生成当天指定长度的ID (自动过期)
     * <p>
     * length = 5 ,当天日期2020年12月12日
     * 结果: 20201212 00001
     * </p>
     *
     * @param key    the key
     * @param length the length
     * @return the string
     */
    public String generateToday(String key, Integer length) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        long num = counter.incrementAndGet();
        counter.expireAt(getTodayEndTime());
        return TimeUtil.getCurrentDatetime(XTime.DAY_NONE) + String.format("%0" + length + "d", num);
    }


    private static Date getTodayEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }
}
