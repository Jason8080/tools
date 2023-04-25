package cn.gmlee.tools.base;

import cn.gmlee.tools.base.enums.Int;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CharUtil;
import cn.gmlee.tools.base.util.DesUtil;
import cn.gmlee.tools.base.util.ExceptionUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * 加密算法.
 * <p>
 * 实际上是DES加密算法
 * </p>
 */
@Deprecated
public class CipherUtil {


    /**
     * 加密.
     *
     * @param content the content
     * @param keys    the keys
     * @return the string
     */
    public static String encode(String content, String key, String... keys) {
        try {
            SecretKey secureKey = DesUtil.generate(getSecretKey(key, keys));
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance(DesUtil.DES);
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, DesUtil.sr);
            // 正式执行加密操作
            byte[] bytes = cipher.doFinal(content.getBytes());
            return null;
        } catch (Exception e) {
            return ExceptionUtil.cast("Cipher 加密异常", e);
        }
    }

    private static String getSecretKey(String key, String... keys) {
        StringBuilder sb = new StringBuilder(key);
        if (BoolUtil.notEmpty(keys)) {
            for (String str : keys) {
                sb.append(str);
            }
        }
        return CharUtil.replenish(sb.toString(), Int.EIGHT);
    }
}
