package cn.gmlee.tools.cache2.kit;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计工具.
 */
public class StatKit {

    private static final Logger log = LoggerFactory.getLogger(StatKit.class);

    private static final Map<String, Hit> map = new HashMap();

    /**
     * The type Hit.
     */
    static class Hit {
        /**
         * 缓存命中次数.
         */
        public long hit;
        /**
         * 缓存未命中次数.
         */
        public long miss;
        /**
         * 访问总次数
         */
        public long total;
        /**
         * 命中率.
         */
        public double rate;
        /**
         * 平均耗时.
         */
        public long elapsedTime;
    }

    /**
     * 命中率统计.
     *
     * @param cache       the cache
     * @param result      the result
     * @param field       the field
     * @param kv         the kv
     * @param elapsedTime the elapsed time
     */
    public static void hitRate(Cache cache, Object result, Field field, Kv<Boolean, Object> kv, long elapsedTime) {
        log.info("----------------------------------------------------------------------------------------------------");
        String key = CacheKit.generateKey(cache, result, field);
        Hit hit = count(key, kv.getKey(), elapsedTime);
        log.info("缓存表名：{}", cache.table());
        log.info("填充属性：{}", field.getName());
        log.info("填充内容：{}", kv.getVal());
        log.info("当前耗时：{}(ms)", elapsedTime);
        log.info("平均耗时：{}(ms)", hit.elapsedTime);
        log.info("总访问次：{}", hit.total);
        log.info("命中次数：{}", hit.hit);
        log.info("命中概率：{}%", hit.rate * 100);
    }

    private static Hit count(String key, Boolean val, long elapsedTime) {
        Hit hit = map.get(key);
        if (hit == null) {
            hit = new Hit();
            hit.elapsedTime = elapsedTime;
            map.put(key, hit);
        }
        hit.total++;
        if (Boolean.TRUE.equals(val)) {
            // 命中次数+1
            hit.hit++;
        } else {
            // 未命中次数+1
            hit.miss++;
        }
        hit.elapsedTime = (hit.elapsedTime + elapsedTime) / 2;
        hit.rate = hit.hit / (double) hit.total;
        return hit;
    }

}
