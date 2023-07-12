package cn.gmlee.tools.api.sign;

import cn.gmlee.tools.api.assist.SignAssist;
import cn.gmlee.tools.base.enums.XCode;
import cn.gmlee.tools.base.ex.SkillException;
import cn.gmlee.tools.base.util.SignUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

/**
 * 通用鉴权控制器
 * <p>
 *     由于@ModelAttribute的原理是采用动态代理
 *     所以该类中被继承的方法不可私有化 (否则注入不了对象)
 * </p>
 *
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
@Validated
public class SignController {

    @Resource
    protected SignAssist signAssist;

    @ModelAttribute
    public void signPre(
            @NotEmpty(message = "应用码是空") @RequestHeader(value = "appid", required = false) String appId,
            @NotEmpty(message = "时间戳是空") @RequestHeader(value = "timestamp", required = false) String timestamp,
            @NotEmpty(message = "随机数是空") @RequestHeader(value = "nonce", required = false) String nonce,
            @NotEmpty(message = "签名是空") @RequestHeader(value = "signature", required = false) String signature,
            HttpServletRequest request, HttpServletResponse response
    ){
        // 签名效验
        if(!timestampOK(timestamp)){
            throw new SkillException(XCode.API_SIGN.code, "签名过期");
        }
        if(!nonceOK(nonce, signature)){
            throw new SkillException(XCode.API_SIGN.code, "重复请求");
        }
        String secretKey = getSecretKey(appId);
        if(StringUtils.isEmpty(secretKey)){
            throw new SkillException(XCode.API_SIGN.code, "暂无私钥");
        }
        if(!SignUtil.check(request, secretKey, signAssist.getExtraHeaders())){
            throw new SkillException(XCode.API_SIGN.code, "非法请求");
        }
    }

    public String getSecretKey(String appId) {
        return signAssist.getAppKeys().get(appId);
    }

    public String setSecretKey(String appId, String secretKey) {
        signAssist.getAppKeys().put(appId, secretKey);
        return secretKey;
    }

    private boolean nonceOK(String nonce, String signature) {
        // 暂不启用nonce重放防控机制
        return true;
    }

    private boolean timestampOK(String timestamp) {
        return System.currentTimeMillis() < Long.valueOf(timestamp);
    }
}
