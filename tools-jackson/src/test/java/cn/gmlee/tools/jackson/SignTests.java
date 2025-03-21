package cn.gmlee.tools.jackson;

import cn.gmlee.tools.base.mod.Kv;
import cn.gmlee.tools.base.util.JsonUtil;
import cn.gmlee.tools.base.util.LocalDateTimeUtil;
import cn.gmlee.tools.base.util.RsaUtil;
import cn.gmlee.tools.base.util.SignUtil;
import cn.gmlee.tools.jackson.anno.Codec;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jas°
 * @date 2021/3/20 (周六)
 */
public class SignTests {
    public static void main(String[] args) {

        // ---------------------
        Kv<String, String> kv = RsaUtil.generateSecretKey();
        System.setProperty("tools.jackson.codec.default.publicKey", kv.getKey());
        System.setProperty("tools.jackson.codec.default.privateKey", kv.getVal());
        // ---------------------

        Map<String, Object> map = new HashMap();
        map.put("appId", 111);
        System.out.println("appId: " + map.get("appId"));
        map.put("nonce", "123");
        System.out.println("nonce: " + map.get("nonce"));
        map.put("timestamp", 7952313600000L);
        System.out.println("timestamp: " + map.get("timestamp"));
        Aa aa = new Aa();
        aa.setA3(new Aa());
        map.put("body", aa);
        String json = JsonUtil.toJson(aa);
        System.out.println("加密: " + json);
        System.out.println("解密: " + JsonUtil.toBean(json, Aa.class));
        System.out.println("body: " + JsonUtil.toJson(map.get("body")));
        String signature = SignUtil.sign(map, "dGT6IKVQL9oSOlOJSGcZnPPCv39z9mHU");
        map.put("signature", signature);
        System.out.println(SignUtil.check(map, "dGT6IKVQL9oSOlOJSGcZnPPCv39z9mHU"));
    }

    @Data
    // 顺序如下: (a=Fri Aug 09 10:28:35 CST 2024, a1=2024-08-09T10:28:35.152, b=b, b1=b1, a2=10, i=0, c=9)
    public static class Aa {
        private Date a = new Date("Fri Aug 09 10:28:35 CST 2024");
        private LocalDateTime a1 = LocalDateTimeUtil.toLocalDateTime(a);
        private String b = "b";
        @Codec
        private String b1 = "b1";
        private BigDecimal a2 = BigDecimal.TEN;
        private int i = 0;
        private Long c = 9L;
        private Aa a3;
    }
}
