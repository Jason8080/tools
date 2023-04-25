package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用状态实体
 *
 * @author Jas°
 * @date 2021/1/11 (周一)
 */
@Getter
@Setter
@ToString
public class Status extends Id {
    /**
     * -9destroy  -8recycle -7delete -6history -5freeze -4expire -3trim -2reset -1init
     * 0 no
     * 1 yes 2skip 3wait 4underway 5confirm 6finish 7close 8rollback 9override
     */
    public Integer status;
    /**
     * 状态名
     */
    public String statusName;
}
