package cn.gmlee.tools.cache2.handler;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.cache2.adapter.FieldAdapter;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.anno.Cache2;
import cn.gmlee.tools.cache2.config.Cache2Conf;
import cn.gmlee.tools.cache2.enums.DataType;
import cn.gmlee.tools.cache2.kit.StatKit;
import cn.gmlee.tools.cache2.server.cache.CacheServer;
import cn.gmlee.tools.cache2.server.ds.DsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
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
     * @param conf the conf
     * @param kvs  the fields map
     */
    public void handler(Cache2Conf conf, List<Kv<Field, Object>> kvs) {
        Iterator<Kv<Field, Object>> it = kvs.iterator();
        while (it.hasNext()) {
            Kv<Field, Object> next = it.next();
            Field field = next.getKey();
            Object result = next.getVal();
            Cache cache = field.getAnnotation(Cache.class);
            Cache2 cache2 = field.getAnnotation(Cache2.class);
            // 启用配置: 两者都用以cache为准
            if (cache == null && cache2 != null && conf != null) {
                cache = newCache(conf, cache2);
            }
            if (!cache.enable()) {
                continue;
            }
            long start = System.currentTimeMillis();
            Kv<Boolean, Object> kv = loading(result, field, cache);
            long end = System.currentTimeMillis();
            StatKit.hitRate(conf, cache, result, field, kv, end - start);
        }
    }

    /**
     * 缓存字段填充逻辑.
     *
     * @param result the result
     * @param field  the field
     * @param cache  the cache
     * @return object kv
     */
    public Kv<Boolean, Object> loading(Object result, Field field, Cache cache) {
        Kv<Boolean, Object> kv = getValue(result, field, cache);
        if (kv.getVal() != null) {
            ClassUtil.setValue(result, field, kv.getVal());
        }
        return kv;
    }

    /**
     * 获取缓存字段值.
     *
     * @param result
     * @param field
     * @param cache
     * @return
     */
    private Kv<Boolean, Object> getValue(Object result, Field field, Cache cache) {
        Kv<Boolean, Object> hit = new Kv<>(true, null);
        // 从缓存获取
        List<Map<String, Object>> list = cacheServer.get(cache, result, field);
        if (BoolUtil.isEmpty(list)) {
            // 记录命中率
            hit.setKey(false);
            // 缓存为空，则从数据源获取
            list = get(result, field, cache);
            // 缓存开启条件
            if (BoolUtil.notEmpty(list) && cache.enable() && cache.expire() != 0L) {
                cacheServer.save(cache, result, field, list);
            }
        }
        Object val = adapter(cache, result, field, list);
        hit.setVal(val);
        return hit;
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

    private static Cache newCache(Cache2Conf conf, Cache2 cache2) {
        return new Cache() {
            @Override
            public String table() {
                // 自定义数据源
                if (BoolUtil.notEmpty(cache2.table())) {
                    return cache2.table();
                }
                // 默认采用配置
                AssertUtil.notEmpty(conf.getTable(), "tools.cache2.table is empty !");
                return conf.getTable();
            }

            @Override
            public String where() {
                // 自定义数据源
                if (BoolUtil.notEmpty(cache2.value())) {
                    return cache2.value();
                }
                // 默认采用配置
                AssertUtil.notEmpty(conf.getWhere(), "tools.cache2.where is empty !");
                return conf.getWhere();
            }

            @Override
            public String key() {
                AssertUtil.notEmpty(conf.getKey(), "tools.cache2.key is empty !");
                return conf.getKey();
            }

            @Override
            public String get() {
                AssertUtil.notEmpty(conf.getGet(), "tools.cache2.get is empty !");
                return conf.getGet();
            }

            @Override
            public String put() {
                AssertUtil.notEmpty(conf.getPut(), "tools.cache2.put is empty !");
                return conf.getPut();
            }

            @Override
            public DataType dataType() {
                return conf.getDataType();
            }

            @Override
            public boolean enable() {
                return conf.getEnable();
            }

            @Override
            public long expire() {
                return conf.getExpire();
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Cache.class;
            }
        };
    }
}
