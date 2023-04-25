package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.define.Payload;
import cn.gmlee.tools.base.enums.Int;
import lombok.Data;

import java.util.Base64;
import java.util.Map;

/**
 * 通用Jwt工具.
 * <p>
 * 支持自定义头部/载体
 * 但只支持载体解码, 如要在头部自定义请自行解码
 * </p>
 *
 * @author Jas°
 * @date 2021/9/16 (周四)
 */
public class JwtUtil {

    public static final String separator = ".";

    public static final Head head = head("JWT", "HS256");

    @Data
    public static class Jwt {
        private String head;
        private String payload;
        private String signature;

        public Jwt(String jwt) {
            Jwt that = getJwt(jwt);
            this.head = that.head;
            this.payload = that.payload;
            this.signature = that.signature;
        }

        public Jwt(String head, String payload, String signature) {
            this.head = head;
            this.payload = payload;
            this.signature = signature;
        }

        private static Jwt getJwt(String jwt) {
            String[] split = jwt.split("\\.");
            AssertUtil.eq(split.length, Int.THREE, String.format("不是Jwt内容: %s", jwt));
            return new Jwt(split[0], split[1], split[2]);
        }

        @Override
        public String toString() {
            return head + separator + payload + separator + signature;
        }
    }

    public static <H extends Head, P extends Payload> String generate(H head, P payload, String key) {
        // 编码头部
        String eh = Base64.getEncoder().encodeToString(head.toString().getBytes());
        // 编码体部
        String ep = Base64.getEncoder().encodeToString(JsonUtil.toJson(payload).getBytes());
        // 加密签名
        String sign = MacUtil.encode(head.getAlg(), eh + separator + ep, key);
        return new Jwt(eh, ep, sign).toString();
    }

    public static <H extends Head, P extends Payload> String generate(P payload, String key) {
        return generate(head, payload, key);
    }

    public static <H extends Head, P extends Payload> String generate(Long uid, Long exp, String key) {
        return generate(head, payload(uid, exp), key);
    }

    public static <H extends Head, P extends Payload> String generate(Long uid, String key) {
        return generate(head, payload(uid, 0L), key);
    }

    // -----------------------------------------------------------------------------------------------------------------


    public static Payload get(String jwt, String key) {
        return get(jwt, key, true);
    }

    public static Payload get(String jwt, String key, boolean check) {
        if (BoolUtil.notEmpty(jwt)) {
            String payload = getPayload(jwt, key);
            Map<String, String> map = JsonUtil.toBean(payload, Map.class);
            Long uid = ExceptionUtil.sandbox(() -> Long.valueOf(map.get("uid")));
            Long exp = ExceptionUtil.sandbox(() -> Long.valueOf(map.get("exp")));
            expire(exp, check);
            return payload(uid, exp);
        }
        return null;
    }

    private static void expire(Long exp, boolean check) {
        // 检查是否过期
        if (check && exp != 0) {
            AssertUtil.gte(exp, TimeUtil.getCurrentTimestamp(), String.format("内容已过期"));
        }
    }

    private static String getPayload(String token, String key) {
        // 根据.符号切割成3段
        Jwt jwt = new Jwt(token);
        // 将第1段解析出头部信息
        Head head = getHead(jwt);
        // 根据头部信息加密签名
        String sign = MacUtil.encode(head.getAlg(), jwt.head + separator + jwt.payload, key);
        // 对比第3段(签名)是否一致
        AssertUtil.eq(jwt.signature, sign, String.format("内容被篡改"));
        return new String(Base64.getDecoder().decode(jwt.payload));
    }

    private static Head getHead(Jwt jwt) {
        String dh = new String(Base64.getDecoder().decode(jwt.head));
        String[] split = dh.split("\\.");
        AssertUtil.eq(split.length, Int.TWO, String.format("内容被篡改"));
        return new Head(split[0], split[1]);
    }

    public static <P extends Payload> P get(String jwt, String key, Class<P> clazz) {
        String payload = getPayload(jwt, key);
        P p = JsonUtil.toBean(payload, clazz);
        expire(p.getExp(), true);
        return p;
    }

    public static <P extends Payload> P get(String jwt, String key, Class<P> clazz, boolean check) {
        String payload = getPayload(jwt, key);
        P p = JsonUtil.toBean(payload, clazz);
        expire(p.getExp(), check);
        return p;
    }


    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 构建头部信息.
     *
     * @param typ
     * @param alg
     * @return
     */
    public static Head head(String typ, String alg) {
        return new Head(typ, alg);
    }

    public static Payload payload(Long uid, Long exp) {
        return new Payload() {
            @Override
            public Long getUid() {
                return uid;
            }

            @Override
            public Long getExp() {
                return exp;
            }
        };
    }


    @Data
    public static class Head {
        private String typ;
        private String alg;

        public Head() {
        }

        public Head(String typ, String alg) {
            this.typ = typ;
            this.alg = alg;
        }

        @Override
        public String toString() {
            return typ + separator + alg;
        }
    }
}
