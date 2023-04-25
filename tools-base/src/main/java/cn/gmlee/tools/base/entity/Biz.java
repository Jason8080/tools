package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 通用业务实体
 *
 * @author Jas°
 * @date 2021/1/22 (周五)
 */
@Getter
@Setter
@ToString
public class Biz implements Serializable {
    /**
     * 业务编号
     */
    public Long bizId;
    /**
     * 业务类型
     */
    public Long bizType;
    /**
     * 业务名称
     */
    public String bizName;
}
