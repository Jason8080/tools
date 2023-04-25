package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限: 树.
 *
 * @author Jas
 */
@Data
public class Option<Code> implements Serializable {
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
     * 权限标识
     * <p>
     * 组织结构行权限: 100(Long)、444(Long)
     * 用户信息列权限: id_card(String)、mobile(String)
     * </p>
     */
    private Code code;
    /**
     * 下级 (列权限一般没有下级)
     */
    private List<Option> options;
}