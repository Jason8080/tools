package cn.gmlee.tools.base.mod;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用用户实体
 *
 * @author Jas°
 * @date 2020/9/27 (周日)
 */
@Data
public class UserInfo implements Serializable {
    protected Long id;
    protected String username;
    protected String nickname;
    protected String portrait;
    protected String realName;
    protected String mobile;
    protected String idCard;
    protected Integer userType;
    protected Integer gender;
    protected Integer status;
}
