package cn.gmlee.tools.base.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 通用排序实体
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class Sort extends Id {
    /**
     * 序号
     */
    @NotNull(message = "序号是空")
    public Integer sort;
}
