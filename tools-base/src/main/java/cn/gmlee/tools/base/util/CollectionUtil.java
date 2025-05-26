package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;

import java.util.*;

/**
 * 集合工具
 *
 * @author Jas °
 * @date 2020 /9/18 (周五)
 */
public class CollectionUtil {
    /**
     * 添加元素.
     *
     * @param <T>      the type parameter
     * @param ts       the ts
     * @param elements the elements
     * @return the collection
     */
    public static <T> Collection<T> addAll(Collection<T> ts, T... elements) {
        if (BoolUtil.notNull(ts) && BoolUtil.notEmpty(elements)) {
            Collections.addAll(ts, elements);
        }
        return ts;
    }

    /**
     * 替换元素.
     * <p>
     * 注意: 在原集合上进行替换
     * </p>
     *
     * @param <T>    the type parameter
     * @param ts     the ts
     * @param oldVal the old val
     * @param newVal the new val
     * @return the t [ ]
     */
    public static <T> Collection<T> replace(Collection<T> ts, T oldVal, T newVal) {
        if (BoolUtil.notEmpty(ts)) {
            ts.remove(oldVal);
            ts.add(newVal);
        }
        return ts;
    }

    /**
     * 替换元素.
     * <p>
     * 注意: 生成新数组; 且长度保持一致
     * </p>
     *
     * @param <T>    the type parameter
     * @param ts     the ts
     * @param oldVal the old val
     * @param newVal the new val
     * @return the t [ ]
     */
    public static <T> List<T> replace(T[] ts, T oldVal, T newVal) {
        List<T> newTs = new ArrayList<>();
        if (BoolUtil.notEmpty(ts)) {
            for (int i = 0; i < ts.length; i++) {
                if (BoolUtil.eq(ts[i], oldVal)) {
                    newTs.add(newVal);
                } else {
                    newTs.add(ts[i]);
                }
            }
        }
        return newTs;
    }

    /**
     * 删除元素.
     * <p>
     * 注意: 生成新数组
     * </p>
     *
     * @param <T>   the type parameter
     * @param ts    the ts
     * @param value the old val
     * @return the t [ ]
     */
    public static <T> Collection<T> remove(Collection<T> ts, T value) {
        if (BoolUtil.notEmpty(ts)) {
            for (T t : ts) {
                if (!BoolUtil.eq(t, value)) {
                    ts.remove(value);
                }
            }
        }
        return ts;
    }

    /**
     * 删除元素.
     * <p>
     * 注意: 生成新数组
     * </p>
     *
     * @param <T>   the type parameter
     * @param ts    the ts
     * @param value the old val
     * @return the t [ ]
     */
    public static <T> List<T> remove(List<T> ts, T value) {
        if (BoolUtil.notEmpty(ts)) {
            for (T t : ts) {
                if (!BoolUtil.eq(t, value)) {
                    ts.remove(value);
                }
            }
        }
        return ts;
    }

    /**
     * 删除元素.
     * <p>
     * 注意: 生成新数组
     * </p>
     *
     * @param <T>   the type parameter
     * @param ts    the ts
     * @param value the old val
     * @return the t [ ]
     */
    public static <T> List<T> remove(T[] ts, T value) {
        List<T> newTs = new ArrayList<>();
        if (BoolUtil.notEmpty(ts)) {
            for (int i = 0; i < ts.length; i++) {
                if (!BoolUtil.eq(ts[i], value)) {
                    newTs.add(ts[i]);
                }
            }
        }
        return newTs;
    }


    /**
     * 批量删除keys.
     *
     * @param map  the map
     * @param keys the keys
     */
    public static void removeKeys(Map map, Object... keys) {
        if (BoolUtil.notEmpty(map) && BoolUtil.notEmpty(keys)) {
            for (Object key : keys) {
                map.remove(key);
            }
        }
    }

    /**
     * Gets ignore case.
     *
     * @param headers the headers
     * @param key     the key
     * @return the ignore case
     */
    public static String getIgnoreCase(Map<String, String> headers, String key) {
        Set<Map.Entry<String, String>> entries = headers.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            if(BoolUtil.equalsIgnoreCase(key, entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 返回第1个值.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the v
     */
    public static <K, V> V first(Map<K, V> map) {
        if (BoolUtil.isEmpty(map)) {
            return null;
        }
        return map.values().stream().findFirst().get();
    }

    /**
     * 根据条件过滤键值对.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @param run the run
     */
    public static <K, V> void filter(Map<K, V> map, Function.Two2r<K, V, Boolean> run) {
        if (BoolUtil.isEmpty(map)) {
            return;
        }
        for (Map.Entry<K, V> next : new HashMap<>(map).entrySet()) {
            K key = next.getKey();
            V val = next.getValue();
            Boolean ok = ExceptionUtil.sandbox(() -> run.run(key, val));
            if (!ok) {
                map.remove(key);
            }
        }
    }

    /**
     * 将Map按照Key升序排序.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return map map
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> keySort(Map<K, V> map) {
        return keySort(map, Map.Entry.comparingByKey());
    }

    /**
     * 将Map按照Key倒序排序.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the tree map
     */
    public static <K extends Comparable, V> Map<K, V> keyReverseSort(Map<K, V> map) {
        return keySort(map, Map.Entry.comparingByKey(Comparator.reverseOrder()));
    }


    private static <K extends Comparable<? super K>, V> Map<K, V> keySort(Map<K, V> map, Comparator<? super Map.Entry<K, V>> comparable) {
        Map<K, V> sortedMap = new LinkedHashMap<>();
        // 对map的key进行排序
        map.entrySet().stream().sorted(comparable).forEach(entry -> {
            V value = entry.getValue();
            // 如果value也是一个Map，递归排序
            if (value instanceof Map) {
                // 递归调用时转换类型
                Map<K, V> newValue = ExceptionUtil.sandbox(() -> (Map<K, V>) value, false);
                sortedMap.put(entry.getKey(), newValue != null ? (V) keySort(newValue, comparable) : value);
            } else {
                sortedMap.put(entry.getKey(), value);
            }
        });
        return sortedMap;
    }

    /**
     * 将value转成string
     *
     * @param <T>         the type parameter
     * @param map         the map
     * @param excludeNull the exclude null
     * @return map map
     */
    public static <T> Map<String, String> valueToString(Map<String, T> map, boolean excludeNull) {
        Map<String, String> all = new HashMap<>(map.size());
        map.forEach((k, v) -> {
            if (v != null) {
                all.put(k, v.toString());
            } else if (!excludeNull) {
                all.put(k, null);
            }
        });
        return all;
    }

    /**
     * Merge map.
     *
     * @param <K>  the type parameter
     * @param <V>  the type parameter
     * @param maps the maps
     * @return the map
     */
    public static <K, V> Map<K, V> merge(Map<K, V>... maps) {
        Map<K, V> all = new HashMap<>();
        if (BoolUtil.isEmpty(maps)) {
            return all;
        }
        for (Map<K, V> map : maps) {
            all.putAll(map);
        }
        return all;
    }

    /**
     * Merge collection.
     *
     * @param <T> the type parameter
     * @param cs  the cs
     * @return the collection
     */
    public static <T> Collection<T> merge(Collection<T>... cs) {
        List<T> all = new ArrayList<>();
        if (BoolUtil.isEmpty(cs)) {
            return all;
        }
        for (Collection c : cs) {
            all.addAll(c);
        }
        return all;
    }

    /**
     * Merge collection.
     *
     * @param <T> the type parameter
     * @param ts  the ts
     * @return the collection
     */
    public static <T> Collection<T> merge(T[]... ts) {
        List<T> all = new ArrayList<>();
        if (BoolUtil.isEmpty(ts)) {
            return all;
        }
        for (T[] t : ts) {
            addAll(all, t);
        }
        return all;
    }

    /**
     * 合并数组.
     *
     * @param <T> the type parameter
     * @param t   the t
     * @param ts  the ts
     * @return 返回新数组 ; 可能返回null (t 或 ts为空时)
     */
    public static <T> T[] merge(T[] t, T... ts) {
        if (BoolUtil.isEmpty(t)) {
            return ts;
        }
        if (BoolUtil.isEmpty(ts)) {
            return t;
        }
        T[] newTs = (T[]) java.lang.reflect.Array.newInstance(t.getClass().getComponentType(), t.length + ts.length);
        System.arraycopy(t, 0, newTs, 0, t.length);
        System.arraycopy(ts, 0, newTs, t.length, ts.length);
        return newTs;
    }

    /**
     * 合并集合.
     *
     * @param <T> the type parameter
     * @param t   the t
     * @param ts  the ts
     * @return 返回新集合 ; 可能返回null (t 或 ts为空时)
     */
    public static <T> Collection<T> merge(Collection<T> t, T... ts) {
        if (BoolUtil.isEmpty(t)) {
            return Arrays.asList(ts);
        }
        if (BoolUtil.isEmpty(ts)) {
            return t;
        }
        List<T> newTs = new ArrayList<>(t.size() + ts.length);
        newTs.addAll(t);
        newTs.addAll(Arrays.asList(ts));
        return newTs;
    }

    /**
     * 替换键.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @param run 新键解析
     */
    public static <K, V> void keyReplace(Map<K, V> map, Function.Two2r<K, V, K> run) {
        if (BoolUtil.isEmpty(map)) {
            return;
        }
        Set<K> keys = new HashMap(map).keySet();
        Iterator<K> it = keys.iterator();
        while (it.hasNext()) {
            K k = it.next();
            V v = map.get(k);
            // 返回新键
            K key = ExceptionUtil.suppress(() -> run.run(k, v));
            if (Objects.equals(k, key)) {
                continue;
            }
            map.remove(k);
            map.put(key, v);
        }
    }

    /**
     * 替换值.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @param run 新值解析
     */
    public static <K, V> void valReplace(Map<K, V> map, Function.Two2r<K, V, V> run) {
        if (BoolUtil.isEmpty(map)) {
            return;
        }
        Set<K> keys = new HashMap(map).keySet();
        Iterator<K> it = keys.iterator();
        while (it.hasNext()) {
            K k = it.next();
            V v = map.get(k);
            // 返回新键
            V val = ExceptionUtil.suppress(() -> run.run(k, v));
            if (Objects.equals(v, val)) {
                continue;
            }
            map.put(k, val);
        }
    }

    /**
     * 找私有元素.
     *
     * @param <K> the type parameter
     * @param ks  the ks
     * @return set 返回每个集合中私有的元素 (即在其他集合中不存在).
     */
    public static <K> Set<K> privateKeys(Collection<K>... ks) {
        // 存储所有元素的集合
        Set<K> all = new HashSet<>();

        // 遍历所有集合并加入all
        for (Collection<K> c : ks) {
            all.addAll(c);
        }

        // 存储差集结果
        Set<K> sets = new HashSet<>();

        // 遍历每个集合，找出只在该集合中的元素
        for (Collection<K> c : ks) {
            for (K element : c) {
                boolean isUnique = true;
                // 检查该元素是否在其他集合中存在
                for (Collection<K> other : ks) {
                    if (other != c && other.contains(element)) {
                        isUnique = false;
                        break;
                    }
                }
                // 如果该元素只在当前集合中，添加到结果集中
                if (isUnique) {
                    sets.add(element);
                }
            }
        }

        return sets;
    }

    /**
     * 找公有元素.
     *
     * @param <K> the type parameter
     * @param ks  the ks
     * @return set 返回每个集合中公有的元素 (即在其他集合中均存在).
     */
    public static <K> Set<K> publicKeys(Collection<K>... ks) {
        Set<K> sets = new HashSet<>(ks[0]);

        for (Collection<K> c : ks) {
            sets.retainAll(c);
        }

        return sets;
    }
}
