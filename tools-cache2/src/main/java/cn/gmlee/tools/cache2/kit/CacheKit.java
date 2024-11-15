package cn.gmlee.tools.cache2.kit;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.Md5Util;
import cn.gmlee.tools.cache2.anno.Cache;
import cn.gmlee.tools.cache2.enums.DataType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * The type Cache kit.
 */
public class CacheKit {

    /**
     * Generate key string.
     *
     * @param cache the cache
     * @param result
     * @param field
     * @return the string
     */
    public static synchronized String generateKey(Cache cache, Object result, Field field) {
        Map<String, Object> obj = ClassKit.generateMapUseCache(result);
        String target = cache.target();
        String key = cache.key();
        String put = cache.put();
        String val = String.format("%s", obj.get(put));
        DataType dataType = cache.dataType();
        String where = ElKit.parse(cache.where(), obj);
        long expire = cache.expire();
        return Md5Util.encode(target, key, put, val, where, dataType.name(), String.valueOf(expire));
    }

    /**
     * Generate key string.
     *
     * @param cache  the cache
     * @param result the result
     * @param field  the field
     * @param list
     * @return the string
     */
    public static String toString(Cache cache, Object result, Field field, List list) {
        String target = cache.target();
        String key = cache.key();
        String get = cache.get();
        DataType dataType = cache.dataType();
        String put = cache.put();
        String where = cache.where();
        StringBuilder sb = new StringBuilder();
        sb.append("缓存来源: ").append(dataType.name()).append("\r\n");
        sb.append("缓存主体: ").append(target).append("\r\n");
        sb.append("缓存外键: ").append(key).append("\r\n");
        sb.append("上传字段: ").append(put).append("\r\n");
        sb.append("下载字段: ").append(get).append("\r\n");
        sb.append("填充字段: ").append(field.getName()).append("\r\n");
        sb.append("缓存关系: ").append(put + "=" + key).append("\r\n");
        sb.append("填充类名: ").append(result.getClass().getName()).append("\r\n");
        sb.append("匹配条件: ").append(where).append("\r\n");
        sb.append("匹配结果: ").append(BoolUtil.isEmpty(list) ? "无" : list.get(0)).append("\r\n");
        return sb.toString();
    }
}
