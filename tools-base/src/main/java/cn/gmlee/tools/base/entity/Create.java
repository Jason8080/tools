package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 通用创建信息
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class Create extends Id {
    /**
     * 创建人
     */
    public Long createBy;
    /**
     * 创建时间
     */
    public Date createAt;
    /**
     * 创建人名称
     */
    public String creator;
}
