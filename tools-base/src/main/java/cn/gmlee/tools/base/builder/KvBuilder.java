package cn.gmlee.tools.base.builder;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.BoolUtil;

import java.util.*;

/**
 * Kv快速构建工具.
 *
 * @author Jas
 */
public class KvBuilder {
    /**
     * Kv快速构建工具.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param k   the k
     * @param v   the v
     * @param os  the os
     * @return the list
     */
    public static <K, V> List<Kv<K, V>> build(K k, V v, Object... os) {
        Map<K, V> map = MapBuilder.build(k, v, os);
        List<Kv<K, V>> kvs = new ArrayList();
        map.forEach((key, val) -> kvs.add(new Kv(key, val)));
        return kvs;
    }

    /**
     * Build list.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the list
     */
    public static <K, V> List<Kv<K, V>> build(Map<K, V> map) {
        if(BoolUtil.isEmpty(map)){
            return null;
        }
        List<Kv<K, V>> kvs = new ArrayList(map.size());
        Iterator<K> it = map.keySet().iterator();
        for (int i = 0; it.hasNext(); i++){
            K key = it.next();
            kvs.add(new Kv(key, map.get(key)));
        }
        return kvs;
    }

    /**
     * Kv快速构建工具.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param k   the k
     * @param v   the v
     * @param os  the os
     * @return the kv [ ]
     */
    public static <K, V> Kv<K, V>[] array(K k, V v, Object... os) {
        List<Kv<K, V>> build = build(k, v, os);
        return build.toArray(new Kv[]{});
    }

    /**
     * Array kv [ ].
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the kv [ ]
     */
    public static <K, V> Kv<K, V>[] array(Map<K, V> map) {
        if(BoolUtil.isEmpty(map)){
            return null;
        }
        Kv[] kvs = new Kv[map.size()];
        Iterator<K> it = map.keySet().iterator();
        for (int i = 0; it.hasNext(); i++){
            K key = it.next();
            kvs[i] = new Kv(key, map.get(key));
        }
        return kvs;
    }
}
