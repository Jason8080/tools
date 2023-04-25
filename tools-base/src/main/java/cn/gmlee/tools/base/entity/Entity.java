package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 通用基础实体类.
 * <p>
 *     常用与表设计时的内建字段.
 * </p>
 *
 * @author Jas°
 * @date 2021/9/3 (周五)
 */
@Getter
@Setter
@ToString
public class Entity implements Serializable {
    /**
     * 主键.
     */
    public Long id;

    /**
     * 状态.
     */
    public Integer status;
    /**
     * 状态名称.
     */
    public String statusName;


    /**
     * 权限主键.
     */
    public Long authId;
    /**
     * 权限类型.
     */
    public String authType;
    /**
     * 权限名称.
     */
    public String authName;

    /**
     * 创建人.
     */
    public Long createBy;
    /**
     * 创建时间.
     */
    public Date createAt;
    /**
     * 创建人名称.
     */
    public String creator;

    /**
     * 修改人.
     */
    public Long updateBy;
    /**
     * 修改时间.
     */
    public Date updateAt;
    /**
     * 修改人名称.
     */
    public String updater;

    /**
     * 是否删除.
     */
    public Boolean del;
}
