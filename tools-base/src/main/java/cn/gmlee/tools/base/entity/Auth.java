package cn.gmlee.tools.base.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 通用权限信息.
 *
 * @author Jas°
 * @date 2021/9/3 (周五)
 */
@Getter
@Setter
@ToString
public class Auth implements Serializable {
    private Long authId;
    /**
     * 权限类型: user(个人), user_group(用户组), role(角色), post(岗位), department(部门), sub_company(子公司), company(公司), group(集团)
     */
    private String authType;
    private String authName;
}
