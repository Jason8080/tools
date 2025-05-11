package cn.gmlee.tools.base.builder;

import cn.gmlee.tools.base.util.AssertUtil;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Map快速构建工具.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author Jas
 */
public class MapBuilder<K, V> extends HashMap<K, V> {
    /**
     * Instantiates a new Map builder.
     */
    public MapBuilder() {
        super();
    }

    /**
     * Instantiates a new Map builder.
     *
     * @param k the k
     * @param v the v
     */
    public MapBuilder(K k, V v) {
        super();
        add(k,v);
    }

    /**
     * Add map builder.
     *
     * @param k the k
     * @param v the v
     * @return the map builder
     */
    public MapBuilder<K, V> add(K k, V v) {
        this.put(k, v);
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }


    /**
     * Map快速构建工具.
     *
     * @param <V> the type parameter
     * @param <K> the type parameter
     * @param k   the k
     * @param v   the v
     * @return the map builder
     */
    public static <V, K> MapBuilder<K, V> of(K k, V v) {
        return new MapBuilder(k, v);
    }

    /**
     * Of map builder.
     *
     * @param <O> the type parameter
     * @param os  the os
     * @return the map builder
     */
    public static <O> MapBuilder<O, O> of(O... os) {
        if(BoolUtil.isEmpty(os)){
            return new MapBuilder<>();
        }
        AssertUtil.eq(os.length % 2, 0, "请检查入参数量(键值成对)");
        return MapBuilder.build(os[0], os[1], Arrays.copyOfRange(os, 2, os.length));
    }

    /**
     * Map快速构建工具.
     *
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @param k   键
     * @param v   值
     * @param os  键值对
     * @return 键值对集合 map
     */
    public static <K, V> MapBuilder<K, V> build(K k, V v, Object... os) {
        AssertUtil.allNotNull(String.format("首对键值是空"), k, v);
        Class<?> kClass = k.getClass();
        Class<?> vClass = v.getClass();
        MapBuilder<K, V> build = MapBuilder.of(k, v);
        if (BoolUtil.isEmpty(os)) {
            return build;
        }
        K key = null;
        boolean skip = true;
        for (int i = 0; i < os.length; i++) {
            Object o = os[i];
            // K
            if (i % 0x2 == 0) {
                if (o == null || BoolUtil.eq(o.getClass(), kClass)) {
                    key = (K) o;
                    skip = false;
                }
            } else {
                // V
                if (o == null || BoolUtil.eq(o.getClass(), vClass)) {
                    K finalKey = key;
                    QuickUtil.isFalse(skip, () -> build.add(finalKey, (V) o));
                }
                skip = true;
            }
        }
        return build;
    }
}
