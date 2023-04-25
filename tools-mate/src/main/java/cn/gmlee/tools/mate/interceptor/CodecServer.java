package cn.gmlee.tools.mate.interceptor;

import java.lang.reflect.Field;
import java.util.Map;

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
    String ENCODE = "encode";
    /**
     * The constant DECODE.
     */
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
        if (DECODE.equalsIgnoreCase(codec)) {
            return true;
        }
        return false;
    }

    /**
     * Encode.
     *
     * @param fieldsMap the fields map
     * @param obj       the obj
     * @param key       the key
     * @param field     the field
     */
    default void encode(Map<String, Field> fieldsMap, Object obj, String key, Field field) {
    }

    /**
     * Encode.
     *
     * @param valuesMap the values map
     * @param obj       同{@param valuesMap}
     * @param key       the key
     * @param value     the value
     */
    default void encode(Map<String, Object> valuesMap, Object obj, String key, Object value) {
    }

    /**
     * Decode.
     *
     * @param fieldsMap the fields map
     * @param obj       the obj
     * @param key       the key
     * @param field     the field
     */
    default void decode(Map<String, Field> fieldsMap, Object obj, String key, Field field) {
    }

    /**
     * Decode.
     *
     * @param valuesMap the fields map
     * @param obj       同{@param valuesMap}
     * @param key       the key
     * @param field     the field
     */
    default void decode(Map<String, Object> valuesMap, Object obj, String key, Object field) {
    }
}
