package cn.gmlee.tools.base.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 对象深拷贝工具.
 */
public class CopyUtil {
    /**
     * Get t.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the t
     */
    public static <T> T get(T object) {
        try {
            // 输出对象流
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
            // 读取对象流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Deep copy error", e);
        }
    }

    /**
     * Get list.
     *
     * @param <T> the type parameter
     * @param os  the os
     * @return the list
     */
    public static <T> List<T> get(Collection<T> os) {
        // 空组不处理
        if (BoolUtil.isEmpty(os)) {
            return new ArrayList();
        }
        // 创建新集合
        List<T> list = new ArrayList(os);
        for (int i = 0; i < list.size(); i++) {
            T o = list.get(i);
            list.set(i, get(o));
        }
        return list;
    }

    /**
     * Get t [ ].
     *
     * @param <T> the type parameter
     * @param os  the os
     * @return the t [ ]
     */
    public static <T> T[] get(T... os) {
        // 空组不处理
        if (BoolUtil.isEmpty(os)) {
            return os;
        }
        // 创建新数组
        T[] newOs = Arrays.copyOf(os, os.length);
        for (int i = 0; i < newOs.length; i++) {
            newOs[i] = get(os[i]);
        }
        return newOs;
    }
}
