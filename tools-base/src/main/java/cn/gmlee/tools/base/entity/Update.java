package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 通用修改信息
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class Update extends Create {
    /**
     * 更新人
     */
    public Long updateBy;
    /**
     * 更新时间
     */
    public Date updateAt;
    /**
     * 更新人名称
     */
    public String updater;
}
