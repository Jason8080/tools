package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Jas°
 */
@Data
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
        super.put(key, val);
    }

    public Kv(String name, K key, V val) {
        this.name = name;
        this.key = key;
        this.val = val;
        super.put(key, val);
    }

    public Kv(K key, V val, String name, String desc) {
        this.key = key;
        this.val = val;
        this.name = name;
        this.desc = desc;
        super.put(key, val);
    }

    @Override
    public String toString() {
        return "Kv{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", key=" + key +
                ", val=" + val +
                ", map=" + super.toString() +
                '}';
    }
}
