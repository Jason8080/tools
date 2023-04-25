package cn.gmlee.tools.base.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

/**
 * 进制转换工具.
 * <p>
 * 常用的进制包含2、10、16
 * </p>
 */
public class HexUtil {

    /**
     * 最大支持36进制
     */
    private static final char[] HEX36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();


    /**
     * 数字转字节数组.
     *
     * @param num the num
     * @return the string
     */
    public static byte[] bytes(long num) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        ExceptionUtil.suppress(() -> dos.writeLong(num));
        return out.toByteArray();
    }

    /**
     * 字节数组转数字.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static long num(byte... bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        long num = buffer.getLong();

        return num;
    }

    /**
     * 字节数组转16进制.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String hex16(byte... bytes) {
        char arr[] = new char[bytes.length * 2];
        int i = 0;
        for (byte bt : bytes) {
            arr[i++] = HEX36[bt >>> 4 & 0xf];
            arr[i++] = HEX36[bt & 0xf];
        }
        return new String(arr);
    }

    /**
     * 16进制转字节数组.
     *
     * @param hex16 the hex 16
     * @return the string
     */
    public static byte[] hex16(String hex16) {
        int length = hex16.length();
        byte[] result;
        if (length % 2 == 1) {
            // 奇数
            length++;
            result = new byte[(length / 2)];
            hex16 = "0" + hex16;
        } else {
            // 偶数
            result = new byte[(length / 2)];
        }
        int j = 0;
        for (int i = 0; i < length; i += 2) {
            result[j] = (byte) Integer.parseInt(hex16.substring(i, i + 2), 16);
            j++;
        }
        return result;
    }
}
