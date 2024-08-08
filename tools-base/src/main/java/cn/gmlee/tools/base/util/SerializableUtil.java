package cn.gmlee.tools.base.util;

import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;

/**
 * 序列化与反序列化工具.
 *
 * @author Jas °
 * @date 2021 /8/12 (周四)
 */
public class SerializableUtil implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(SerializableUtil.class);

    /**
     * 序列化.
     *
     * @param <T> the type parameter
     * @param t   the t
     * @return byte array output stream
     * @throws IOException the io exception
     */
    public static <T> ByteArrayOutputStream serialize(T t) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (BoolUtil.notNull(t)) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                oos.writeObject(t);
            } catch (Exception e) {
                ExceptionUtil.cast(String.format("序列化失败"), e);
            }
        }
        return stream;
    }

    /**
     * 反序列化.
     *
     * @param bytes the bytes
     * @return the object
     */
    public static Object deserialize(byte[] bytes) {
        return deserialize(bytes, Object.class, false);
    }

    /**
     * 反序列化.
     *
     * @param bytes   the bytes
     * @param allowEx 是否允许抛出异常
     * @return the object
     */
    public static Object deserialize(byte[] bytes, Boolean allowEx) {
        return deserialize(bytes, Object.class, allowEx);
    }

    /**
     * 反序列化.
     *
     * @param bytes the bytes
     * @param clazz the clazz
     * @return the object
     */
    public static Object deserialize(byte[] bytes, Class<T> clazz) {
        return deserialize(bytes, clazz, false);
    }

    /**
     * 反序列化.
     *
     * @param <T>     the type parameter
     * @param bytes   the bytes
     * @param clazz   the clazz
     * @param allowEx the allow ex
     * @return the object
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz, Boolean allowEx) {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            ObjectInputStream oos = new ObjectInputStream(stream);
            return (T) oos.readObject();
        } catch (Throwable throwable) {
            if (allowEx) {
                return ExceptionUtil.cast(String.format("反序列化失败"), throwable);
            }
        }
        return null;
    }

    /**
     * 序列化编码.
     *
     * @param <T> the type parameter
     * @param t   the t
     * @return the string
     */
    public static <T> String encoder(T t) {
        ByteArrayOutputStream stream = serialize(t);
        return Base64.getMimeEncoder().encodeToString(stream.toByteArray());
    }

    /**
     * 反序列化解码.
     *
     * @param str the str
     * @return the object
     */
    public static Object decode(String str) {
        String target = NullUtil.get(str);
        return decode(target, Object.class);
    }

    /**
     * 反序列化解码.
     *
     * @param <T>   the type parameter
     * @param str   the str
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T decode(String str, Class<T> clazz) {
        byte[] bytes = Base64.getMimeDecoder().decode(str.getBytes());
        return deserialize(bytes, clazz, false);
    }
}
