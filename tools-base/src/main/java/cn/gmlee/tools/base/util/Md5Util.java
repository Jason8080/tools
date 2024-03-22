package cn.gmlee.tools.base.util;

import java.security.MessageDigest;
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
        return encode(getBytes(content, salt));
    }

    /**
     * 直接加密.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String encode(byte... bytes) {
        MessageDigest md5 = ExceptionUtil.suppress(
                () -> MessageDigest.getInstance(MD5)
        );
        return hex(md5.digest(bytes));
    }

    private static String base64(byte[] bytes) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] newBytes = encoder.encode(bytes);
        return new String(newBytes);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
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

    public static void main(String[] args) {
        System.out.println(Md5Util.encode());
    }

    private static byte[] getBytes(String content, String... os) {
        StringBuilder sb = new StringBuilder(content);
        for (String o : os) {
            sb.append(o);
        }
        String toString = sb.toString();
        return toString.getBytes();
    }
}
