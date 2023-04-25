package cn.gmlee.tools.kv;

import java.util.Date;

/**
 * 伪Kv工具.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 * @author Jas °
 * @date 2021 /7/20 (周二)
 */
public interface FakeKv<K,V> {
    /**
     * 防并发存储.
     *
     * @param k      the k
     * @param v      the v
     * @param expire the expire
     * @return the nx
     */
    Boolean setNx(K k, V v, Date expire);

    /**
     * 写入值.
     *
     * @param k the k
     * @param v the v
     * @return boolean
     */
    Boolean set(K k, V v);

    /**
     * 写入值.
     *
     * @param k      the k
     * @param v      the v
     * @param expire the expire
     * @return boolean
     */
    Boolean setEx(K k, V v, Date expire);

    /**
     * 获取值.
     *
     * @param k the k
     * @return the v
     */
    V get(K k);

    /**
     * 删除值.
     *
     * @param k the k
     */
    void delete(K k);
}
