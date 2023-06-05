package cn.gmlee.tools.mate.interceptor;

/**
 * 编解码服务.
 * <p>
 * 默认编解码采用SnUtil方案, 可重写替换.
 * </p>
 */
public interface CodecServer {
    /**
     * The constant ENCODE.
     */
// 编码总开关
    String ENCODE = "encode";
    /**
     * The constant ENCODE_STRING.
     */
// 入参spring编码开关: update(@Param("mobile") String mobile);
    String ENCODE_STRING = "encode_string";
    /**
     * The constant DECODE.
     */
// 解码总开关
    String DECODE = "decode";

    /**
     * 编解码开关.
     *
     * @param codec the codec
     * @return the boolean
     */
    default boolean enable(String codec) {
        if (ENCODE.equalsIgnoreCase(codec)) {
            return true;
        }
        if (ENCODE_STRING.equalsIgnoreCase(codec)) {
            // 默认不开启
            return false;
        }
        if (DECODE.equalsIgnoreCase(codec)) {
            return true;
        }
        return false;
    }

    /**
     * 编码.
     *
     * @param obj 可能是空(String入参场景)
     * @param key 字段名
     * @param val 字段值
     * @return the string
     */
    default String encode(Object obj, String key, Object val) {
        return null;
    }

    /**
     * 解码.
     *
     * @param obj 对象
     * @param key 字段名
     * @param val 字段值
     * @return the string
     */
    default String decode(Object obj, String key, Object val) {
        return null;
    }
}
