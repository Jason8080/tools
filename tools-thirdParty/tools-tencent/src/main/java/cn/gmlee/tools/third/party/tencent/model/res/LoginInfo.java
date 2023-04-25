package cn.gmlee.tools.third.party.tencent.model.res;

import lombok.Data;

import java.io.Serializable;

/**
 * 小程序登陆响应实体
 *
 * @author Jas°
 * @date 2020/10/12 (周一)
 */
@Data
public class LoginInfo implements Serializable {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}
