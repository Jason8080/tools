package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * 数据权限: 树.
 *
 * @author Jas
 */
@Data
public class Option<ID> implements Tree<Option,ID>, Serializable {
    /**
     * 应用编号.
     */
    private Long appId;
    /**
     * 权限名称
     * <p>
     * 组织结构行权限: 巨微科技(中国)、巨微科技(日本)
     * 用户信息列权限: 身份证号码、手机号码
     * </p>
     */
    private String name;
    /**
     * 权限编码
     * <p>
     * 个人权限: create_by、部门权限: dept_id、部门(包含所有下级)权限: dept_ids、全部权限: true
     * </p>
     */
    private String code;
    /**
     * 权限标识
     * <p>
     * 组织结构行权限: 100(Long)、444(Long)
     * 用户信息列权限: id_card(String)、mobile(String)
     * </p>
     */
    private ID id;
    /**
     * 上级标识
     */
    private ID parentId;
    /**
     * 下级 (列权限一般没有下级)
     */
    private Collection<Option> children;
}