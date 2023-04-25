package cn.gmlee.tools.base.util;

/**
 * 字节操作工具.
 */
public class ByteUtil {
    /**
     * 空字节.
     */
    public static final byte[] empty = {};

    /**
     * 字节合并.
     *
     * @param t1 the t1
     * @param t2 the t2
     * @return the byte [ ]
     */
    public static byte[] merge(byte[] t1, byte... t2) {
        if (t2 == null || t2.length < 1) {
            return t1;
        }
        byte[] bytes = new byte[t1.length + t2.length];
        for (int i = t1.length; i < bytes.length; i++) {
            bytes[i] = t2[i - t1.length];
        }
        return bytes;
    }


    /**
     * 将4个字节转成十进制数字.
     * <p>
     * 多余的高位将被忽略.
     * </p>
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int asInt(byte... bytes) {
        int sum = 0;
        for (int i = bytes.length, j = 0; i > 0 && j < 4; i--, j++) {
            sum += bytes[i - 1] << ((bytes.length - i) * 8);
        }
        return sum;
    }


    /**
     * 将8个字节转成十进制数字.
     * <p>
     * 多余的高位将被忽略.
     * </p>
     *
     * @param bytes the bytes
     * @return the int
     */
    public static long asLong(byte... bytes) {
        long sum = 0;
        for (int i = bytes.length, j = 0; i > 0 && j < 8; i--, j++) {
            sum += ((long) bytes[i - 1]) << ((bytes.length - i) * 8);
        }
        return sum;
    }

    /**
     * 将数字转成4个字节.
     *
     * @param num the num
     * @return the byte [ ]
     */
    public static byte[] asBytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (num >> 0x18);
        bytes[1] = (byte) (num >> 0x10);
        bytes[2] = (byte) (num >> 0x8);
        bytes[3] = (byte) (num);
        return bytes;
    }

    /**
     * As bytes byte [ ].
     *
     * @param num the num
     * @return the byte [ ]
     */
    public static byte[] asBytes(long num) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (num >> 0x38);
        bytes[1] = (byte) (num >> 0x30);
        bytes[2] = (byte) (num >> 0x28);
        bytes[3] = (byte) (num >> 0x20);
        bytes[4] = (byte) (num >> 0x18);
        bytes[5] = (byte) (num >> 0x10);
        bytes[6] = (byte) (num >> 0x8);
        bytes[7] = (byte) (num);
        return bytes;
    }

    /**
     * 比较两个数值的大小.
     *
     * @param target the target
     * @param source the source
     * @return the int
     */
    public static int compareTo(byte[] target, byte[] source) {
        if (BoolUtil.isEmpty(target) && BoolUtil.isEmpty(source)) {
            return 0;
        }
        if (!BoolUtil.isEmpty(target) && BoolUtil.isEmpty(source)) {
            return 1;
        }
        if (BoolUtil.isEmpty(target) && !BoolUtil.isEmpty(source)) {
            return -1;
        }
        // 都非空的话分为两个步骤比较
        int diff = target.length - source.length;
        // 数组长度一致
        if (diff == 0) {
            return zeroDiffCompareTo(target, source);
        }
        // target
        if (diff > 0) {
            // 1. 检查多余的数据有效
            for (int i = 0; i < Math.abs(diff); i++) {
                if (target[i] > 0) {
                    return 1;
                }
            }
            // 2. 比较剩余的数据大小
            byte[] bytes = new byte[source.length];
            System.arraycopy(target, Math.abs(diff), bytes, 0, bytes.length);
            return compareTo(bytes, source);
        }
        // source
        if (diff < 0) {
            // 1. 检查多余的数据有效
            for (int i = 0; i < Math.abs(diff); i++) {
                if (source[i] > 0) {
                    return -1;
                }
            }
            // 2. 比较剩余的数据大小
            byte[] bytes = new byte[target.length];
            System.arraycopy(source, Math.abs(diff), bytes, 0, bytes.length);
            return compareTo(target, bytes);
        }
        return 0;
    }

    /**
     * Compare to int.
     *
     * @param target the target
     * @param source the source
     * @return the int
     */
    public static int compareTo(byte target, byte source) {
        return target - source;
    }

    private static int zeroDiffCompareTo(byte[] target, byte[] source) {
        AssertUtil.eq(target.length, source.length, String.format("说好的零差异比较呢?"));
        for (int i = 0; i < Math.max(target.length, source.length); i++) {
            if (compareTo(target[i], source[i]) > 0) {
                return 1;
            } else if (compareTo(target[i], source[i]) < 0) {
                return -1;
            }
            continue;
        }
        return 0;
    }
}
