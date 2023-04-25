package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用用户设置
 *
 * @author Jas°
 * @date 2020/9/27 (周日)
 */
@Data
public class DataInfo<K, V> implements Serializable {
    protected Long id;
    protected K key;
    protected V value;
}
