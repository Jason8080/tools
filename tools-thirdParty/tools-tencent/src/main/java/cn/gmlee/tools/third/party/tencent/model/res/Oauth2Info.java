package cn.gmlee.tools.third.party.tencent.model.res;

import lombok.Data;

import java.io.Serializable;

/**
 * 公众号登陆响应实体
 *
 * @author Jas°
 * @date 2020/10/12 (周一)
 */
@Data
public class Oauth2Info extends ErrorInfo implements Serializable {
    private String openid;
    private String scope;
    private Long expires_in;
    private String access_token;
    private String refresh_token;
}
