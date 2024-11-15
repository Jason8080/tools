package cn.gmlee.tools.cache2.kit;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.BigDecimalUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.config.Cache2Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统计工具.
 */
public class StatKit {

    private static final Logger log = LoggerFactory.getLogger(StatKit.class);

    private static final Map<String, Hit> map = new ConcurrentHashMap<>();

    /**
     * The type Hit.
     */
    static class Hit {
        /**
         * 缓存命中次数.
         */
        public AtomicLong hit = new AtomicLong(0);
        /**
         * 缓存未命中次数.
         */
        public AtomicLong miss = new AtomicLong(0);
        /**
         * 访问总次数
         */
        public AtomicLong total = new AtomicLong(0);
        /**
         * 命中率.
         */
        public BigDecimal rate;
        /**
         * 平均耗时.
         */
        public long elapsedTime;
    }

    /**
     * 命中率统计.
     *
     * @param conf
     * @param cache       the cache
     * @param result      the result
     * @param field       the field
     * @param kv          the kv
     * @param elapsedTime the elapsed time
     */
    public static void hitRate(Cache2Conf conf, Cache cache, Object result, Field field, Kv<Boolean, Object> kv, long elapsedTime) {
        String key = CacheKit.generateKey(cache, result, field);
        Hit hit = count(key, kv.getKey(), elapsedTime);
        if ((conf == null) || Boolean.TRUE.equals(conf.isLog())) {
            log.info("--------------------------------------------------------------------------------------------------");
            log.info("缓存主题：{}", cache.target());
            log.info("填充属性：{}", field.getName());
            log.info("填充内容：{}", kv.getVal());
            log.info("当前命中：{}", kv.getKey());
            log.info("当前耗时：{}(ms)", elapsedTime);
            log.info("平均耗时：{}(ms)", hit.elapsedTime);
            log.info("总访问次：{}", hit.total);
            log.info("命中次数：{}", hit.hit);
            log.info("命中概率：{}%", BigDecimalUtil.multiply(hit.rate, BigDecimalUtil.ONE_HUNDRED).setScale(BigDecimalUtil.SCALE_2));
        }
    }

    private static Hit count(String key, Boolean val, long elapsedTime) {
        Hit hit = map.get(key);
        if (hit == null) {
            synchronized (map) {
                hit = map.get(key);
                if (hit == null) {
                    hit = new Hit();
                    hit.elapsedTime = elapsedTime;
                    map.put(key, hit);
                }
            }
        }
        synchronized (hit) {
            hit.total.incrementAndGet();
            if (Boolean.TRUE.equals(val)) {
                hit.hit.incrementAndGet();
            } else {
                hit.miss.incrementAndGet();
            }
            hit.elapsedTime = (hit.elapsedTime + elapsedTime) / 2;
            hit.rate = rate(hit);
        }
        return hit;
    }

    private static BigDecimal rate(Hit hit) {
        BigDecimal current = new BigDecimal(hit.hit.get());
        BigDecimal total = new BigDecimal(hit.total.get());
        return current.divide(total, BigDecimalUtil.SCALE_4, RoundingMode.DOWN);
    }

}
