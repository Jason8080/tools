package cn.gmlee.tools.base.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * The type Md 5.
 *
 * @author Jas °
 */
public class Md5Util {

    /**
     * The constant MD5.
     */
    public static final String MD5 = "MD5";

    /**
     * 加密.
     *
     * @param content 明文
     * @param salt    盐
     * @return 密文 string
     */
    public static String encode(String content, String... salt) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(MD5);
            byte[] bytes = md5.digest(getBytes(content, salt));
            return hex(bytes);
        } catch (NoSuchAlgorithmException e) {
            return ExceptionUtil.cast(e);
        }
    }

    private static String base64(byte[] bytes) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] newBytes = encoder.encode(bytes);
        return new String(newBytes);
    }

    private static String hex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            int number = b & 0xff;
            String s = Integer.toHexString(number);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private static byte[] getBytes(String content, String... os) {
        StringBuilder sb = new StringBuilder(content);
        for (int i = 0; i < os.length; i++) {
            sb.append(os[i]);
        }
        String toString = sb.toString();
        return toString.getBytes();
    }
}
