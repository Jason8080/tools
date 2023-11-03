package cn.gmlee.tools.cache2.server.cache;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.kit.CacheKit;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
public class RedisCacheServer extends AbstractCacheServer {

    @Value("${tools.cache2.key:TOOLS:CACHE2:KEY_}")
    private String cacheKey;

    @Autowired
    private RedisTemplate<String, List> redis;

    @Override
    public List<Map<String, Object>> get(Cache cache, Object result, Field field) {
        String key = CacheKit.generateKey(cache, result, field);
        return redis.opsForValue().get(getKey(key));
    }

    @Override
    public void save(Cache cache, Object result, Field field, List<Map<String, Object>> list) {
        String key = CacheKit.generateKey(cache, result, field);
        if (cache.expire() > 0) {
            redis.opsForValue().set(getKey(key), list, cache.expire(), TimeUnit.SECONDS);
        } else {
            redis.opsForValue().set(getKey(key), list);
        }
    }

    @Override
    public void clear(String... keys) {
        if (BoolUtil.isEmpty(keys)) {
            return;
        }
        for (String key : keys) {
            redis.delete(getKey(key));
        }
    }

    public String getKey(String key) {
        return cacheKey.concat(key);
    }
}
