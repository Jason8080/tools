package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用用户设置
 * <p>
 * 区别于SettingInfo, 它用于直接使用: 控制配置项
 * </p>
 *
 * @param <V> the type parameter
 * @author Jas°
 * @date 2020 /9/27 (周日)
 */
@Data
public class ConfigInfo<V> implements Serializable {
    protected Long id;
    protected String name;
    protected String code;
    protected V value;
}
