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
    // 编码总开关
    String ENCODE = "encode";
    // 入参spring编码开关: update(@Param("mobile") String mobile);
    String ENCODE_STRING = "encode_string";
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
     * <p>
     *     当mapper处理的数据集为对象或集合对象时触发
     * </p>
     *
     * @param fieldsMap 持久化对象反射后的字段集
     * @param obj       持久化对象
     * @param key       字段名称
     * @param field     字段
     */
    default void encode(Map<String, Field> fieldsMap, Object obj, String key, Field field) {
    }

    /**
     * 编码.
     * <p>
     *     当mapper处理的数据集为Map时触发
     * </p>
     *
     * @param valuesMap 持久化数据集
     * @param obj       同{@param valuesMap}
     * @param key       字段名
     * @param value     字段值
     */
    default void encode(Map<String, Object> valuesMap, Object obj, String key, Object value) {
    }

    /**
     * 解码.
     * <p>
     *     当mapper处理的数据集为对象或集合对象时触发
     * </p>
     *
     * @param fieldsMap 反射后的字段集
     * @param obj       持久化对象
     * @param key       字段名
     * @param field     字段
     */
    default void decode(Map<String, Field> fieldsMap, Object obj, String key, Field field) {
    }

    /**
     * 解码.
     * <p>
     *     当mapper处理的数据集为Map时触发
     * </p>
     *
     * @param valuesMap 持久化数据集
     * @param obj       同{@param valuesMap}
     * @param key       字段名
     * @param value     字段值
     */
    default void decode(Map<String, Object> valuesMap, Object obj, String key, Object value) {
    }
}
