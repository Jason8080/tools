package cn.gmlee.tools.base.mod;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Jas°
 */
@Getter
@Setter
@ToString
public class Kv<K,V> extends HashMap<K,V> implements Serializable {
    private String name;
    private String desc;
    private K key;
    private V val;

    public Kv() {
    }

    public Kv(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public Kv(String name, K key, V val) {
        this.name = name;
        this.key = key;
        this.val = val;
    }

    public Kv(String name, String desc, K key, V val) {
        this.name = name;
        this.desc = desc;
        this.key = key;
        this.val = val;
    }
}
