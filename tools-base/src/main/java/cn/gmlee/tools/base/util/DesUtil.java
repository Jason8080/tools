package cn.gmlee.tools.base.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Des加密解密工具
 * 根据密钥加密/解密
 * 密钥长度是8的整数倍位
 *
 * @author Jas °
 * @date 2021 /3/16 (周二)
 */
public class DesUtil {
    /**
     * The constant DES.
     */
    public static final String DES = "DES";
    /**
     * DES算法要求有一个可信任的随机数源
     */
    public static final SecureRandom sr = new SecureRandom();

    /**
     * 加密.
     *
     * @param content   the content
     * @param secretKey the secret key
     * @return the string
     */
    public static String encode(String content, String secretKey) {
        AssertUtil.gte(secretKey.length(), 8, String.format("DES 加密算法的密钥需要8位长度以上: %s", secretKey.length()));
        try {
            SecretKey secureKey = generate(secretKey);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance(DES);
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, sr);
            // 正式执行加密操作
            byte[] bytes = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return ExceptionUtil.cast("DES 加密异常", e);
        }
    }

    /**
     * 解密.
     *
     * @param content   the content
     * @param secretKey the secret key
     * @return the string
     */
    public static String decode(String content, String secretKey) {
        AssertUtil.gte(secretKey.length(), 8, String.format("DES 加密算法的密钥需要8位长度以上: %s", secretKey.length()));
        try {
            // 从原始密匙数据创建一个DESKeySpec对象
            SecretKey secureKey = generate(secretKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance(DES);
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, secureKey, sr);
            // 正式执行解密操作
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(content.getBytes()));
            return new String(bytes);
        } catch (Exception e) {
            return ExceptionUtil.cast("DES 解密异常", e);
        }
    }

    /**
     * Generate aes secret key string.
     *
     * @return the string
     */
    public static String generate() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(DES);
            return Base64.getEncoder().encodeToString(keyGenerator.generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            return ExceptionUtil.cast("密钥生成异常", e);
        }
    }

    /**
     * Generate secret key.
     *
     * @param secretKey the secret key
     * @return the secret key
     * @throws Exception the exception
     */
    public static SecretKey generate(String secretKey) throws Exception {
        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(secretKey.getBytes());
        // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        return keyFactory.generateSecret(dks);
    }
}
