package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色权限: 角色
 *
 * @author Jas
 */
@Data
public class Role implements Serializable {
    private Long id;
    private String code;
    private String name;

    /**
     * 应用编号
     */
    private Long appId;
}