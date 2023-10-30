package cn.gmlee.tools.cache2.handler;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.cache2.adapter.FieldAdapter;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.kit.StatKit;
import cn.gmlee.tools.cache2.server.cache.CacheServer;
import cn.gmlee.tools.cache2.server.ds.DsServer;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 缓存处理器.
 */
public class CacheHandler {

    private static final Logger log = LoggerFactory.getLogger(CacheHandler.class);

    private static final ThreadLocal<Map<String, List<Map<String, Object>>>> THREAD_LOCAL = new ThreadLocal<>();

    @Resource
    private CacheServer cacheServer;

    @Resource
    private List<DsServer> dsServers = new ArrayList<>();

    @Resource
    private List<FieldAdapter> fsAdapters = new ArrayList<>();

    /**
     * 缓存字段处理器.
     *
     * @param kvs the fields map
     */
    public void handler(List<Kv<Field, Object>> kvs) {
        Iterator<Kv<Field, Object>> it = kvs.iterator();
        while (it.hasNext()) {
            Kv<Field, Object> next = it.next();
            Field field = next.getKey();
            Object result = next.getVal();
            Cache cache = field.getAnnotation(Cache.class);
            if (!cache.enable()) {
                continue;
            }
            long start = System.currentTimeMillis();
            Object val = loading(result, field, cache);
            long end = System.currentTimeMillis();
            StatKit.hitRate(cache, result, field, val, end - start);
        }
    }

    /**
     * 缓存字段填充逻辑.
     *
     * @param result
     * @param field
     * @param cache
     * @return
     */
    public Object loading(Object result, Field field, Cache cache) {
        Object value = getValue(result, field, cache);
        if (value == null) {
            return null;
        }
        boolean old = field.isAccessible();
        QuickUtil.isFalse(old, () -> field.setAccessible(true));
        try {
            ExceptionUtil.suppress(() -> field.set(result, value));
            return value;
        } finally {
            QuickUtil.isFalse(old, () -> field.setAccessible(old));
        }
    }

    /**
     * 获取缓存字段值.
     *
     * @param result
     * @param field
     * @param cache
     * @return
     */
    private Object getValue(Object result, Field field, Cache cache) {
        // 从缓存获取
        List<Map<String, Object>> list = cacheServer.get(cache, result, field);
        if (list == null) {
            // 缓存为空，则从数据源获取
            list = get(result, field, cache);
            // 缓存开启条件
            if (list != null && cache.enable() && cache.expire() != 0L) {
                cacheServer.save(cache, result, field, list);
            }
        }
        return adapter(cache, result, field, list);
    }

    /**
     * 从数据源中获取数据
     *
     * @param result
     * @param field
     * @param cache
     * @return
     */
    private List<Map<String, Object>> get(Object result, Field field, Cache cache) {
        Iterator<DsServer> it = dsServers.iterator();
        while (it.hasNext()) {
            DsServer dsServer = it.next();
            if (!dsServer.support(cache)) {
                continue;
            }
            return dsServer.get(cache, result, field);
        }
        return null;
    }

    /**
     * 适配字段类型.
     *
     * @param cache
     * @param result
     * @param field
     * @param list
     * @return
     */
    private Object adapter(Cache cache, Object result, Field field, List<Map<String, Object>> list) {
        if (BoolUtil.isEmpty(list)) {
            return null;
        }
        for (FieldAdapter adapter : fsAdapters) {
            if (adapter.support(cache, result, field)) {
                return adapter.get(cache, result, field, list);
            }
        }
        return null;
    }
}
