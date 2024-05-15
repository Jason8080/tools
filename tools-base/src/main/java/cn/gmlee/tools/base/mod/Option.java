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
public class Option<Code> implements Tree<Option, Code>, Serializable {
    /**
     * 应用编号.
     */
    private Long appId;
    /**
     * 权限编码
     * <p>
     * 简权限: oneself、dept_id、dept_ids、all </br>
     * 行权限: 保留位 <br/>
     * 列权限: 保留位
     * </p>
     */
    private String code;
    /**
     * 权限名称
     * <p>
     * 非常简单的权限: 仅自己、部门、部门(包含所有下级)、全部</br>
     * 组织结构行权限: 巨微科技(中国)、巨微科技(日本)</br>
     * 用户信息列权限: 身份证号码、手机号码
     * </p>
     */
    private String name;
    /**
     * 权限标识
     * <p>
     * 非常简单的权限: -、100、100101、-</br>
     * 组织结构行权限: 100(Long)、444(Long)</br>
     * 用户信息列权限: id_card(String)、mobile(String)
     * </p>
     */
    private Code id;
    /**
     * 上级标识
     */
    private Code parentId;
    /**
     * 下级 (列权限一般没有下级)
     */
    private Collection<Option> children;
}