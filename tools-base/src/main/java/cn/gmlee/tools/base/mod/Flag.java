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
public class Flag implements Serializable {
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
     * 标签编码
     * <p>
     * 行权限: row-auth-organization、row-auth-city
     * 列权限: column-auth-user、column-auth-car
     * </p>
     */
    private String code;
    /**
     * 权限树
     */
    private List<Option> options;
}