package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 通用主键实体
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class IdString implements Serializable {
    /**
     * 主键
     */
    @NotEmpty(message = "主键是空")
    public String id;
}
