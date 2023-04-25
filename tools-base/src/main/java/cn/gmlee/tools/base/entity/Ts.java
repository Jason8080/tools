package cn.gmlee.tools.base.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用时间戳实体
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class Ts extends Id {
    /**
     * 时间戳
     */
    public Long ts;
}
