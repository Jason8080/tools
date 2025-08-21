package cn.gmlee.tools.base.util;

import java.util.Base64;

/**
 * The type Multi assist.
 */
public class Base64Util {
    /**
     * 编码.
     *
     * @param format the format
     * @param bytes  the bytes
     * @return the string
     */
    public static String encode(String format, byte... bytes) {
        return String.format("data:%s;base64,%s", NullUtil.get(format), new String(Base64.getEncoder().encode(bytes)));
    }

    /**
     * 解码.
     *
     * @param base64 the base 64
     * @return the byte [ ]
     */
    public static byte[] decode(String base64) {
        if(BoolUtil.contain(base64, "base64,")){
            base64 = base64.split("base64,")[1];
        }
        return Base64.getDecoder().decode(base64);
    }
}
