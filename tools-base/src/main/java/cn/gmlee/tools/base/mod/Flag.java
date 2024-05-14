package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限: 标识.
 * <p>
 *     用于区分是什么权限
 * </p>
 *
 * @author Jas
 */
@Data
public class Flag<Code> implements Serializable {
    /**
     * 应用编号.
     */
    private Long appId;
    /**
     * 标签名称
     * <p>
     * 行权限: 组织结构、行政区域
     * 列权限: 用户信息、车辆信息
     * </p>
     */
    private String name;
    /**
     * 标签标识
     * <p>
     * 简权限: simple
     * 行权限: row-organization、row-city
     * 列权限: column-user、column-car
     * </p>
     */
    private Code id;
    /**
     * 标签编码
     * <p>
     * 简权限: 个人 oneself、部门 dept_id、部门(包含全部下级) dept_ids、全部 all..
     * 行权限: create_by、dept_id、dept_ids、merchant_id..
     * 列权限: 暂留扩展位,可能是: table1、table2..
     * </p>
     */
    private String code;
    /**
     * 权限树
     */
    private List<Option> options;
}