package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用用户设置
 * <p>
 * 区别于ConfigInfo, 它用于继承: 可自定义设置项
 * </p>
 *
 * @author Jas°
 * @date 2020/9/27 (周日)
 */
@Data
public class SettingInfo implements Serializable {
    protected Long id;
}
