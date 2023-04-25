package cn.gmlee.tools.cache2.server.cache;

import cn.gmlee.tools.cache2.kit.CacheKit;
import cn.gmlee.tools.cache2.anno.Cache;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
public class RedisCacheServer extends AbstractCacheServer {

    private String cacheKey;

    private RedisTemplate<String, List> redis;

    @Override
    public List<Map<String, Object>> get(Cache cache, Object result, Field field) {
        String key = CacheKit.generateKey(cache, result, field);
        return redis.opsForValue().get(getKey(key));
    }

    @Override
    public void save(Cache cache, Object result, Field field, List<Map<String, Object>> list) {
        String key = CacheKit.generateKey(cache, result, field);
        if(cache.expire() > 0) {
            redis.opsForValue().set(getKey(key), list, cache.expire(), TimeUnit.SECONDS);
        }else {
            redis.opsForValue().set(getKey(key), list);
        }
    }

    public String getKey(String key) {
        return cacheKey.concat(key);
    }
}
