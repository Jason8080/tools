package cn.gmlee.tools.third.party.tencent;

import cn.gmlee.tools.base.util.ZxingUtil;
import cn.gmlee.tools.third.party.tencent.kit.WxPayKit;
import cn.gmlee.tools.third.party.tencent.config.WxPayConfiguration;
import cn.gmlee.tools.third.party.tencent.model.req.WxPayNativeRequest;

import java.io.FileOutputStream;
import java.util.Map;

/**
 * @author Jas°
 * @date 2021/3/1 (周一)
 */
public class PayTests {
    public static void main(String[] args) throws Exception {
        WxPayConfiguration conf = new WxPayConfiguration();
        conf.setAppId("");
        conf.setMchId("");
        conf.setKey("");
        conf.setCurrency("CNY");
        conf.setNotifyUrl("");
        conf.setHttpReadTimeoutMs(2000);
        conf.setHttpConnectTimeoutMs(3000);
        WxPayNativeRequest req = new WxPayNativeRequest();
        req.setIp("218.17.71.58");
        Map<String, String> data = WxPayKit.getRequestData(conf, req);
        Map<String, String> res = WxPayKit.unifiedOrder(conf, data);
        String zxingUrl = WxPayKit.getNativeResponse(res, "");
        FileOutputStream out = new FileOutputStream("C:\\Users\\DELL\\Desktop\\实验室\\QR.jpg");
        ZxingUtil.encode(zxingUrl, null, out, true);
    }
}
