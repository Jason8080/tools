package cn.gmlee.tools.base.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Aes加密解密工具
 * 用密钥加密/解密
 * <p>
 * 密钥长度为8的整数倍, 且最低为16位长度
 *
 * @author Jas °
 */
public class AesUtil {
    /**
     * 注意key和加密用到的字符串是不一样的 加密还要指定填充的加密模式和填充模式 AES密钥可以是128或者256，加密模式包括ECB, CBC等
     * ECB模式是分组的模式，CBC是分块加密后，每块与前一块的加密结果异或后再加密 第一块加密的明文是与IV变量进行异或
     */
    public static final String AES = "AES";
    private static final String ECB_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    /**
     * IV(Initialization Value)是一个初始值，对于CBC模式来说，它必须是随机选取并且需要保密的
     * 而且它的长度和密码分组相同(比如：对于AES 128为128位(标准)，即长度为16的byte类型数组)
     */
    private static final byte[] IV_PARAMETERS_128 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

    /**
     * 加密.
     *
     * @param content   the content
     * @param secretKey the secret key
     * @return the string
     */
    public static String encode(String content, String secretKey) {
        AssertUtil.eq(secretKey.length() % 8, 0, String.format("AES 加密算法仅支持8的整数倍位长度的密钥: %s", secretKey.length()));
        AssertUtil.gt(secretKey.length(), 8, String.format("AES 加密算法的密钥需要8位长度以上(建议16位): %s", secretKey.length()));
        SecretKey key = generateAesSecretKey(secretKey);
        return aesEcbEncode(content.getBytes(), key);
    }

    /**
     * 解密
     *
     * @param content   the content
     * @param secretKey the secret key
     * @return string string
     */
    public static String decode(String content, String secretKey) {
        AssertUtil.eq(secretKey.length() % 8, 0, String.format("AES 加密算法仅支持8的整数倍位长度的密钥: %s", secretKey.length()));
        AssertUtil.gt(secretKey.length(), 8, String.format("AES 加密算法的密钥需要8位长度以上(建议16位): %s", secretKey.length()));
        SecretKey key = generateAesSecretKey(secretKey);
        return aesEcbDecode(content, key);
    }


    /**
     * 使用ECB模式进行加密。 加密过程三步走： 1. 传入算法，实例化一个加解密器 2. 传入加密模式和密钥，初始化一个加密器 3.
     * 调用doFinal方法加密
     *
     * @param plainText
     * @return
     */
    private static String aesEcbEncode(byte[] plainText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(plainText);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            return ExceptionUtil.cast("加密异常", e);
        }
    }

    /**
     * 使用ECB解密，三步走，不说了
     *
     * @param decodedText
     * @param key
     * @return
     */
    private static String aesEcbDecode(String decodedText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(decodedText)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            return ExceptionUtil.cast("解密异常", e);
        }
    }

    /**
     * CBC加密，三步走，只是在初始化时加了一个初始变量
     *
     * @param plainText
     * @param key
     * @param IVParameter
     * @return
     */
    private static String aesCbcEncode(byte[] plainText, SecretKey key, byte[] IVParameter) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IVParameter);
            Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            byte[] bytes = cipher.doFinal(plainText);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            return ExceptionUtil.cast("解密异常", e);
        }
    }

    /**
     * CBC 解密
     *
     * @param decodedText
     * @param key
     * @param IVParameter
     * @return
     */
    private static String aesCbcDecode(String decodedText, SecretKey key, byte[] IVParameter) {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IVParameter);
        try {
            Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(decodedText)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            return ExceptionUtil.cast("解密异常", e);
        }
    }

    /**
     * 1.创建一个KeyGenerator 2.调用KeyGenerator.generateKey方法
     * 由于某些原因，这里只能是128，如果设置为256会报异常，原因如下:
     * 因为某些国家的进口管制限制，Java发布的运行环境包中的加解密有一定的限制。
     * 比如默认不允许256位密钥的AES加解密，解决方法就是修改策略文件。
     *
     * @return secret key
     */
    public static String generateAesSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            return Base64.getEncoder().encodeToString(keyGenerator.generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            return ExceptionUtil.cast("密钥生成异常", e);
        }
    }


    /**
     * 1.创建一个KeyGenerator 2.调用KeyGenerator.generateKey方法
     * 由于某些原因，这里只能是128，如果设置为256会报异常，原因如下:
     * 因为某些国家的进口管制限制，Java发布的运行环境包中的加解密有一定的限制。
     * 比如默认不允许256位密钥的AES加解密，解决方法就是修改策略文件。
     *
     * @param secretKey the secret key
     * @return secret key
     */
    public static SecretKey generateAesSecretKey(String secretKey) {
        AssertUtil.eq(secretKey.length() % 8, 0, String.format("AES 加密算法仅支持8的整数倍位长度的密钥: %s", secretKey.length()));
        AssertUtil.gt(secretKey.length(), 8, String.format("AES 加密算法的密钥需要8位长度以上(建议16位): %s", secretKey.length()));
        return new SecretKeySpec(secretKey.getBytes(), AES);
    }

    /**
     * 还原密钥
     *
     * @param secretKey the secret key
     * @return secret key
     */
    public static String restoreSecretKey(SecretKey secretKey) {
        byte[] encoded = secretKey.getEncoded();
        return new String(encoded);
    }
}
