package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 通用逻辑删除实体
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class Del extends Id {
    /**
     * 是否删除
     */
    @NotNull(message = "删除标识是空")
    public Boolean del;
}
