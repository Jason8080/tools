package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.Function;
import org.omg.CORBA.INTERNAL;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * 返回第1个值.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the v
     */
    public static <K, V> V first(Map<K, V> map){
        if(BoolUtil.isEmpty(map)){
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
     * @return the tree map
     */
    public static <K extends Comparable, V> TreeMap<K, V> keySort(Map<K, V> map) {
        TreeMap<K, V> treeMap = new TreeMap<>();
        if (BoolUtil.notEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
    }

    /**
     * Key reverse sort tree map.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @param map the map
     * @return the tree map
     */
    public static <K extends Comparable, V> TreeMap<K, V> keyReverseSort(Map<K, V> map) {
        TreeMap<K, V> treeMap = new TreeMap<>(Comparator.comparing((K k) -> k).reversed());
        if (BoolUtil.notEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
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
     * 找独有元素.
     *
     * @param <K> the type parameter
     * @param ks  the ks
     * @return the set
     */
    public static <K> Set<K> uniqueKeys(Collection<K>... ks){
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
     * 找共有元素.
     *
     * @param <K> the type parameter
     * @param ks  the ks
     * @return the set
     */
    public static <K> Set<K> commonKeys(Collection<K>... ks){
        Set<K> sets = new HashSet<>(ks[0]);

        for (Collection<K> c : ks) {
            sets.retainAll(c);
        }

        return sets;
    }
}
