package cn.gmlee.tools.third.party.tencent;

import cn.gmlee.tools.third.party.tencent.model.res.TokenInfo;
import cn.gmlee.tools.third.party.tencent.util.AppletUtil;
import cn.gmlee.tools.third.party.tencent.util.WxUtil;

/**
 * 测试Wx工具类
 *
 * @author Jas°
 * @date 2020/10/10 (周六)
 */
public class AppletTests {
    private static final String APP_ID ="wx2b294c5820908199";
    private static final String SECRET ="3627e26cd0fa7b22abc97f41efc11d1d";

    public static void main(String[] args) {
        TokenInfo tokenInfo = WxUtil.getAccessToken(APP_ID, SECRET);
        String qr = AppletUtil.getUnlimitedQr(tokenInfo.getAccess_token(), "123", "");
        System.out.println(qr);
    }
}
