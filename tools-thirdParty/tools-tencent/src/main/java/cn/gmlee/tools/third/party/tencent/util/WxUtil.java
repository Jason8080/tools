package cn.gmlee.tools.third.party.tencent.util;

import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.third.party.tencent.model.res.LoginInfo;
import cn.gmlee.tools.third.party.tencent.model.res.TokenInfo;
import cn.gmlee.tools.third.party.tencent.model.res.TicketInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/**
 * 通用微信客户端工具
 *
 * @author Jas °
 * @date 2021 /2/22 (周一)
 */
public class WxUtil {

    private static Logger logger = LoggerFactory.getLogger(WxUtil.class);

    private static final String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String GET_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
    private static final String CODE_2_SESSION = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 获取小程序全局唯一后台接口调用凭据（access_token）.
     * <p>
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/access-token/auth.getAccessToken.html">详情请查看官网地址</a>
     * </p>
     *
     * @param appId  the app id
     * @param secret the secret
     * @return the string
     */
    public static TokenInfo getAccessToken(String appId, String secret){
        HttpResult httpResult = HttpUtil.get(String.format(GET_ACCESS_TOKEN_URL, appId, secret));
        return httpResult.jsonResponseBody2bean(TokenInfo.class);
    }

    /**
     * 获取微信网页的临时票据 (ticket).
     * <p>
     * <a href="https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/JS-SDK.html#62">详情请查看官网地址</a>
     * </p>
     *
     * @param accessToken the access token
     * @return the ticket info
     */
    public static TicketInfo getTicket(String accessToken){
        HttpResult httpResult = HttpUtil.get(String.format(GET_TICKET_URL, accessToken));
        return httpResult.jsonResponseBody2bean(TicketInfo.class);
    }

    /**
     * 获取登陆信息.
     * <p>
     * <a href="https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code">详情请查看官网地址</a>
     * </p>
     *
     * @param appId  the app id
     * @param secret the secret
     * @param jsCode the js code
     * @return the login info
     */
    public static LoginInfo getLoginInfo(String appId, String secret, String jsCode){
        HttpResult httpResult = HttpUtil.get(String.format(CODE_2_SESSION, appId, secret, jsCode));
        return httpResult.jsonResponseBody2bean(LoginInfo.class);
    }


    /**
     * 获取用户信息中的解密数据工具.
     *
     * @param encryptedData 加密数据
     * @param sessionKey    加密密钥
     * @param iv            偏移量
     * @return 用户信息 user info
     */
    public static Map<String, Object> getUserInfo(String encryptedData, String sessionKey, String iv) {
        // 被加密的数据
        byte[] dataByte = Base64.getDecoder().decode(encryptedData);
        // 加密秘钥
        byte[] keyByte = Base64.getDecoder().decode(sessionKey);
        // 偏移量
        byte[] ivByte = Base64.getDecoder().decode(iv);
        // 如果密钥不足16位，那么就补足.  这个if 中的内容很重要
        int base = 16;
        if (keyByte.length % base != 0) {
            int groups = keyByte.length / base + (keyByte.length % base != 0 ? 1 : 0);
            byte[] temp = new byte[groups * base];
            Arrays.fill(temp, (byte) 0);
            System.arraycopy(keyByte, 0, temp, 0, keyByte.length);
            keyByte = temp;
        }

        try {
            // 初始化
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
            byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                String result = new String(resultByte, "UTF-8");
                return JsonUtil.toBean(result, Map.class);
            }
        } catch (Exception e) {
            logger.info("解析加密的用户数据异常，传参：encryptedData=【{}】,sessionKey=【{}】,iv=【{}】", encryptedData, sessionKey, iv);
            logger.error("WxUtil-->getUserInfo-->解析加密的用户数据异常！", e);
            e.printStackTrace();
        }
        return null;
    }
}
