package cn.gmlee.tools.third.party.tencent.util;

import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.mod.HttpResult;
import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.base.util.HttpUtil;
import cn.gmlee.tools.base.util.ImgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用微信小程序工具
 *
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
@SuppressWarnings("all")
public class AppletUtil {

    private static Logger logger = LoggerFactory.getLogger(AppletUtil.class);

    private static final String GET_UNLIMITED_QR_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s";


    /**
     * 通过该接口生成的小程序码，永久有效，数量暂无限制.
     * <p>
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/qr-code/wxacode.getUnlimited.html">详情请查看官网地址</a>
     * </p>
     *
     * @param accessToken the access token
     * @param scene       the scene
     * @param page        the page
     * @return the string
     */
    public static String getUnlimitedQr(String accessToken, String scene, String page){
        Map<String, Object> params = new HashMap(2);
        params.put("scene", scene);
        params.put("page", page);
        HttpResult httpResult = HttpUtil.post(String.format(GET_UNLIMITED_QR_URL, accessToken), params);
        if(httpResult.isJson()){
            String result = httpResult.byteResponseBody2String();
            String msg = String.format("请确保页面[%s]存在 ", page);
            ExceptionUtil.cast(XCode.THIRD_PARTY3000.code, msg + result);
        }
        return ImgUtil.bytes2base64(httpResult.getResult());
    }

    /**
     * 直接写出 (原始字节流,不是Base64).
     *
     * @param accessToken the access token
     * @param scene       the scene
     * @param page        the page
     * @param out         the out
     */
    public static void getUnlimitedQr(String accessToken, String scene, String page, OutputStream out){
        Map<String, Object> params = new HashMap(2);
        params.put("scene", scene);
        params.put("page", page);
        HttpUtil.post(String.format(GET_UNLIMITED_QR_URL, accessToken), params, out);
    }


}
