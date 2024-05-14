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
     * 权限名称
     * <p>
     * 非常简单的权限: 仅自己场景不需要options、IT部、技术中心(包含IT部等子部门)、全部的场景不需要options</br>
     * 组织结构行权限: 巨微科技(中国)、巨微科技(日本)</br>
     * 用户信息列权限: 身份证号码、手机号码
     * </p>
     */
    private String name;
    /**
     * 权限标识
     * <p>
     * 非常简单的权限: ...、100、100101、...</br>
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