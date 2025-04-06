package cn.gmlee.tools.ai.assist;

import java.util.Base64;

/**
 * The type Multi assist.
 */
public class MultiAssist {
    /**
     * Base 64 image string.
     *
     * @param format the format
     * @param bytes  the bytes
     * @return the string
     */
    public static String base64Image(String format, byte... bytes) {
        return String.format("data:;base64,%s", new String(Base64.getEncoder().encode(bytes)));
    }

    /**
     * Base 64 audio string.
     *
     * @param format the format
     * @param bytes  the bytes
     * @return the string
     */
    public static String base64Audio(String format, byte... bytes) {
        return String.format("data:;base64,%s", new String(Base64.getEncoder().encode(bytes)));
    }

    /**
     * Base 64 video string.
     *
     * @param format the format
     * @param bytes  the bytes
     * @return the string
     */
    public static String base64Video(String format, byte... bytes) {
        return String.format("data:;base64,%s", new String(Base64.getEncoder().encode(bytes)));
    }
}
