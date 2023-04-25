package cn.gmlee.tools.base;

import cn.gmlee.tools.base.util.SignUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jas°
 * @date 2021/3/20 (周六)
 */
public class SignTests {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap();
        map.put("appId", 111);
        System.out.println("appId: " + map.get("appId"));
        map.put("nonce", "123");
        System.out.println("nonce: " + map.get("nonce"));
        map.put("timestamp", 7952313600000L);
        System.out.println("timestamp: " + map.get("timestamp"));
        String signature = SignUtil.sign(map, "dGT6IKVQL9oSOlOJSGcZnPPCv39z9mHU");
        map.put("signature", signature);
        System.out.println("signature: " + map.get("signature"));
        System.out.println(SignUtil.check(map, "dGT6IKVQL9oSOlOJSGcZnPPCv39z9mHU"));
    }
}
