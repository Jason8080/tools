package cn.gmlee.tools.third.party.tencent.util;

import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.third.party.tencent.model.res.Oauth2Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用微信服务号工具
 *
 * @author Jas°
 * @date 2021/2/22 (周一)
 */
public class ServiceUtil {

    private static Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

    private static final String OAUTH2_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";


    /**
     * 公众号登陆授权
     * <p>
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html">详情请查看官网地址</a>
     * </p>
     *
     * @param appId
     * @param secret
     * @param code
     * @return
     */
    public static Oauth2Info oauth2(String appId, String secret, String code) {
        HttpResult httpResult = HttpUtil.get(String.format(OAUTH2_ACCESS_TOKEN, appId, secret, code));
        return httpResult.jsonResponseBody2bean(Oauth2Info.class);
    }
}
