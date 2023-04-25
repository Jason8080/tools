package cn.gmlee.tools.base.util;

import java.security.MessageDigest;

/**
 * 简单加密方式
 * 不需要密钥即可加密解密
 *
 * @author Jas °
 */
public class ShaUtil {
    /**
     * The constant SHA256.
     */
    public static final String SHA256 = "SHA-256";
    /**
     * The constant SHA1.
     */
    public static final String SHA1 = "SHA";

    /**
     * 加密
     *
     * @param str the str
     * @return the string
     */
    public static String encoder(String str) {
        return encoder(str, SHA256);
    }

    /**
     * Encoder string.
     *
     * @param str       the str
     * @param algorithm the algorithm
     * @return the string
     */
    public static String encoder(String str, String algorithm) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(str.getBytes());
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            return ExceptionUtil.cast(String.format("%s 加密异常", algorithm));
        }
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
