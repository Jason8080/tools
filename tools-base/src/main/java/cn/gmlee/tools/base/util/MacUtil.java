package cn.gmlee.tools.base.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 密钥工具类.
 *
 * @author Jas °
 * @date 2021 /9/16 (周四)
 */
public class MacUtil {
    public static final String HS256 = "HmacSHA256";

    /**
     * 密钥编码.
     *
     * @param algorithm the algorithm
     * @param content   the content
     * @param key       the key
     * @return the string
     */
    public static String encode(String algorithm, String content, String key) {
        try {
            SecretKeySpec secureKey = new SecretKeySpec(key.getBytes(), getAlgorithm(algorithm));
            Mac mac = Mac.getInstance(getAlgorithm(algorithm));
            mac.init(secureKey);
            byte[] bytes = mac.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return ExceptionUtil.cast("加密异常", e);
        }
    }

    private static String getAlgorithm(String alg) {
        return "HS256".equalsIgnoreCase(alg) ? HS256 : alg;
    }
}
