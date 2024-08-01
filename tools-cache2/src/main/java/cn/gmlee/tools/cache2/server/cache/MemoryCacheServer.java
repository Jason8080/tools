package cn.gmlee.tools.cache2.server.cache;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.TimeUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.kit.CacheKit;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class MemoryCacheServer extends AbstractCacheServer {

    private Map<String, Expire<List>> memory = new ConcurrentHashMap<>();

    @Override
    public List<Map<String, Object>> get(Cache cache, Object result, Field field) {
        String key = CacheKit.generateKey(cache, result, field);
        Expire<List> expire = memory.get(key);
        if (expire == null) {
            return null;
        }
        if (cache.expire() > 0 && TimeUtil.getCurrentTimestampSecond() > expire.getExpire()) {
            memory.remove(key);
            return null;
        }
        return expire.getObj();
    }

    @Override
    public void save(Cache cache, Object result, Field field, List<Map<String, Object>> list) {
        String key = CacheKit.generateKey(cache, result, field);
        memory.put(key, new Expire(list, cache.expire() + TimeUtil.getCurrentTimestampSecond()));
    }

    @Override
    public void clear(String... keys) {
        if (BoolUtil.isEmpty(keys)) {
            Set<String> set = memory.keySet();
            keys = set.toArray(new String[0]);
        }
        for (String key : keys) {
            memory.remove(key);
        }
    }


    @Data
    class Expire<T> {
        private T obj;
        private long expire;

        public Expire(T list, long expire) {
            this.obj = list;
            this.expire = expire;
        }
    }
}
