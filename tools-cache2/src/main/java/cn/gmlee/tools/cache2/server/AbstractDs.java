package cn.gmlee.tools.cache2.server;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.ClassUtil;
import cn.gmlee.tools.cache2.anno.Cache;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 源数据抽象服务.
 */
@Data
public abstract class AbstractDs implements Ds {

    @Override
    public List<Map<String, Object>> get(Cache cache, Object result, Field field) {
        List<Map<String, Object>> list = list(cache, result, field);
        if (BoolUtil.isEmpty(list)) {
            return list;
        }
        Map<String, Object> retMap = ClassUtil.generateMapUseCache(result);
        Iterator<Map<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            Map<String, Object> dsMap = it.next();
            if (!matching(retMap, dsMap, cache.put(), cache.key())) {
                it.remove();
            }
        }
        return list;
    }

    public boolean matching(Map<String, Object> retMap, Map<String, Object> dsMap, String retKey, String dsKey) {
        if(!BoolUtil.allNotEmpty(retKey, dsKey)){
            return false;
        }
        if(!Objects.equals(retMap.get(retKey), dsMap.get(dsKey))){
            return false;
        }
        return true;
    }

    protected abstract List<Map<String, Object>> list(Cache cache, Object result, Field field);
}
