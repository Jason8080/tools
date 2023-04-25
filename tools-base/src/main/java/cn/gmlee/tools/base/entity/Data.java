package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 通用数据实体
 *
 * @author Jas°
 * @date 2021 /1/22 (周五)
 */
@Getter
@Setter
@ToString
public class Data implements Serializable {
    /**
     * 数据主键
     */
    private Long dataId;
    /**
     * 数据类型
     */
    private Long dataType;
    /**
     * 数据名称
     */
    private String dataName;
}
