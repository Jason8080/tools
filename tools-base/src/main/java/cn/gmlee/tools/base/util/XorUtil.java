package cn.gmlee.tools.base.util;

/**
 * 异或对称加密算法.
 *
 * @author Jas °
 * @date 2021 /7/29 (周四)
 */
public class XorUtil {
    /**
     * 字符加密.
     *
     * @param source the source
     * @param keys   the keys
     * @return the string
     */
    public static String encode(String source, String... keys) {
        byte[] bytes = NullUtil.get(source).getBytes();
        if (BoolUtil.notEmpty(keys)) {
            for (String key : keys) {
                if (BoolUtil.notEmpty(key)) {
                    bytes = encode(bytes, key.getBytes());
                }
            }
        }
        return new String(bytes);
    }

    /**
     * 字符解密.
     *
     * @param target the target
     * @param keys   the keys
     * @return the string
     */
    public static String decode(String target, String... keys) {
        return encode(target, keys);
    }

    /**
     * 字节加密.
     *
     * @param source the source
     * @param keys   the keys
     * @return the byte [ ]
     */
    public static byte[] encode(byte[] source, byte... keys) {
        byte[] target = source;
        if (source != null && source.length > 0 && keys != null && keys.length > 0) {
            for (int i = 0; i < source.length; i++) {
                for (byte key : keys) {
                    target[i] ^= key;
                }
            }
        }
        return target;
    }

    /**
     * 字节解密.
     *
     * @param target the target
     * @param keys   the keys
     * @return the byte [ ]
     */
    public static byte[] decode(byte[] target, byte... keys) {
        return encode(target, keys);
    }

    /**
     * 加密.
     *
     * @param source the source
     * @param keys   the keys
     * @return the long
     */
    public static Long encode(Long source, Long... keys) {
        Long target = source;
        if (BoolUtil.notNull(source) && BoolUtil.notEmpty(keys)) {
            for (Long key : keys) {
                target ^= key;
            }
        }
        return target;
    }

    /**
     * 解密.
     *
     * @param target the target
     * @param keys   the keys
     * @return the long
     */
    public static Long decode(Long target, Long... keys) {
        return encode(target, keys);
    }


    /**
     * 加密.
     *
     * @param source the source
     * @param keys   the keys
     * @return the long
     */
    public static Integer encode(Integer source, Integer... keys) {
        Integer target = source;
        if (BoolUtil.notNull(source) && BoolUtil.notEmpty(keys)) {
            for (Integer key : keys) {
                target ^= key;
            }
        }
        return target;
    }

    /**
     * 解密.
     *
     * @param target the target
     * @param keys   the keys
     * @return the long
     */
    public static Integer decode(Integer target, Integer... keys) {
        return encode(target, keys);
    }
}
