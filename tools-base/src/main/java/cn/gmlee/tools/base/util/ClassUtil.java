package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.anno.Column;
import cn.gmlee.tools.base.mod.Kv;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 通用字节码工具类
 *
 * @author Jas °
 * @date 2020 /9/21 (周一)
 */
public class ClassUtil {

    private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);

    /**
     * 根据类路径空参构造方法创建对象.
     *
     * @param clazz the clazz
     * @return the object
     */
    public static Object newInstance(String clazz) {
        AssertUtil.notEmpty(clazz, String.format("字节码路径是空"));
        try {
            Class<?> aClass = Class.forName(clazz);
            Constructor<?> constructor = aClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * New instance t.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @param os    the os
     * @return the t
     */
    public static <T> T newInstance(Class<T> clazz, Object... os) {
        Class<?>[] parameterTypes = {};
        if (BoolUtil.notEmpty(os)) {
            parameterTypes = new Class[os.length];
            for (int i = 0; i < os.length; i++) {
                parameterTypes[i] = os[i].getClass();
            }
        }
        try {
            Constructor<T> constructor = clazz.getConstructor(parameterTypes);
            return constructor.newInstance(os);
        } catch (Exception e) {
            return ExceptionUtil.cast(e);
        }
    }

    /**
     * 获取当前对象使用的泛型类型.
     * <p>
     * 仅当在父类中获取子类的泛型类型
     * </p>
     *
     * @param <T> the type parameter
     * @param obj the obj
     * @return the generic class
     */
    public static <T> Class<T> getGenericClass(Object obj) {
        Class<?> clazz = obj.getClass();
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType genericSuperclass = (ParameterizedType) type;
            Type actualTypeArgument = genericSuperclass.getActualTypeArguments()[0];
            if (actualTypeArgument instanceof Class) {
                return (Class<T>) actualTypeArgument;
            }
            if (actualTypeArgument instanceof ParameterizedTypeImpl) {
                return (Class<T>) ((ParameterizedTypeImpl) actualTypeArgument).getRawType();
            }
        }
        return null;
    }

    /**
     * 将对象转换成Map (递归所有上级属性, 且当前属性会覆盖上级属性).
     *
     * @param <T>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @return the map
     */
    public static <T, V> Map<String, V> generateMap(T source) {
        return generateMap(source, false);
    }

    /**
     * 将对象转换成Map (递归所有上级属性, 且当前属性会覆盖上级属性).
     *
     * @param <T>        the type parameter
     * @param <V>        the type parameter
     * @param source     the source
     * @param ignoreNull the ignore null
     * @return map map
     */
    public static <T, V> Map<String, V> generateMap(T source, boolean ignoreNull) {
        Map<String, V> all = new LinkedHashMap(0);
        if (BoolUtil.notNull(source)) {
            Class<?> clazz = source.getClass();
            all.putAll(recursionSuperclass(source, clazz.getSuperclass(), ignoreNull));
            // 当前字段覆盖上级字段
            all.putAll(generateCurrentMap(source, clazz, ignoreNull));
        }
        return all;
    }

    /**
     * 将当前对象转换成Map (不包含继承的属性).
     *
     * @param <T>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @return the map
     */
    public static <T, V> Map<String, V> generateCurrentMap(T source) {
        if (BoolUtil.notNull(source)) {
            return generateCurrentMap(source, source.getClass());
        }
        return Collections.emptyMap();
    }

    /**
     * Gets fields map.
     *
     * @param <T>    the type parameter
     * @param source the source
     * @return the fields map
     */
    public static <T> Map<String, Field> getFieldsMap(T source) {
        Map<String, Field> all = new LinkedHashMap(0);
        if (BoolUtil.notNull(source)) {
            Class<?> clazz = source.getClass();
            all.putAll(recursionSuperclass(clazz.getSuperclass()));
            // 当前字段覆盖上级字段
            all.putAll(getCurrentFieldsMap(clazz));
        }
        return all;
    }

    private static <T, V> Map<String, V> recursionSuperclass(T source, Class<?> clazz) {
        return recursionSuperclass(source, clazz, false);
    }

    private static <T, V> Map<String, V> recursionSuperclass(T source, Class<?> clazz, boolean ignoreNull) {
        // 根据JavaBean对象获取对应的字节码对象
        Map<String, V> all = new LinkedHashMap(0);
        if (clazz != null) {
            // 保存当前字段
            all.putAll(generateCurrentMap(source, clazz, ignoreNull));
            // 递归上级字段
            all.putAll(recursionSuperclass(source, clazz.getSuperclass(), ignoreNull));
        }
        return all;
    }

    private static Map<String, Field> recursionSuperclass(Class<?> clazz) {
        // 根据JavaBean对象获取对应的字节码对象
        Map<String, Field> all = new LinkedHashMap(0);
        if (clazz != null) {
            // 保存当前字段
            all.putAll(getCurrentFieldsMap(clazz));
            // 递归上级字段
            all.putAll(recursionSuperclass(clazz.getSuperclass()));
        }
        return all;
    }

    /**
     * 获取当前对象中所有的属性值.
     *
     * @param <T>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @param clazz  the clazz
     * @return the fields
     */
    public static <T, V> Map<String, V> generateCurrentMap(T source, Class<?> clazz) {
        return generateCurrentMap(source, clazz, false);
    }

    /**
     * 获取当前对象中所有的属性值.
     *
     * @param <T>        the type parameter
     * @param <V>        the type parameter
     * @param source     the source
     * @param clazz      the clazz
     * @param ignoreNull 是否忽略空值(指key/val均不可为null)
     * @return the fields
     */
    public static <T, V> Map<String, V> generateCurrentMap(T source, Class<?> clazz, boolean ignoreNull) {
        Map<String, V> map = new LinkedHashMap(0);
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                //打开私有访问
                field.setAccessible(true);
                //获取属性
                String name = field.getName();
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    // 隐藏列
                    if (column.hide()) {
                        continue;
                    }
                    // 别名列
                    if (BoolUtil.notEmpty(column.value())) {
                        name = column.value();
                    }

                }
                //获取属性值
                try {
                    Object value = field.get(source);
                    if (column != null) {
                        // 是否序列化
                        if (column.serializer()) {
                            value = JsonUtil.toJson(value);
                        } else {
                            // 格式化
                            if (value instanceof Date) {
                                value = TimeUtil.format((Date) value, column.dateFormat());
                            }
                            if (value instanceof LocalDateTime) {
                                value = TimeUtil.format((LocalDateTime) value, column.dateFormat());
                            }
                        }
                    }
                    if (!ignoreNull) {
                        map.put(name, (V) value);
                    } else if (name != null && value != null) {
                        map.put(name, (V) value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    field.setAccessible(false);
                }
            }
        }
        return map;
    }

    /**
     * Gets fields map.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the fields map
     */
    public static <T> Map<String, Field> getFieldsMap(Class<?> clazz) {
        Map<String, Field> all = new LinkedHashMap(0);
        if (BoolUtil.notNull(clazz)) {
            all.putAll(recursionSuperclass(clazz.getSuperclass()));
            // 当前字段覆盖上级字段
            all.putAll(getCurrentFieldsMap(clazz));
        }
        return all;
    }

    /**
     * Gets current fields map.
     *
     * @param clazz the clazz
     * @return the current fields map
     */
    public static Map<String, Field> getCurrentFieldsMap(Class<?> clazz) {
        Map<String, Field> map = new LinkedHashMap(0);
        if (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                boolean a = field.isAccessible();
                //打开私有访问
                QuickUtil.isFalse(a, () -> field.setAccessible(true));
                //获取属性
                String name = field.getName();
                //获取属性值
                map.put(name, field);
                QuickUtil.isFalse(a, () -> field.setAccessible(false));
            }
        }
        return map;
    }

    /**
     * 获取指定名称字段:Id的值.
     *
     * @param <T>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @return the id
     */
    public static <T, V> V getId(T source) {
        Map<String, Object> generateMap = generateMap(source);
        return (V) generateMap.get("id");
    }

    /**
     * 获取指定名称字段的值 (无泛型).
     *
     * @param <T>       the type parameter
     * @param source    the source
     * @param fieldName the field name
     * @return field value
     */
    public static <T> Object getFieldValue(T source, String fieldName) {
        Map<String, Object> generateMap = generateMap(source);
        return generateMap.get(fieldName);
    }

    /**
     * Sets value.
     *
     * @param <T>       the type parameter
     * @param source    the source
     * @param fieldName the field name
     * @param value     the value
     */
    public static <T> void setValue(T source, String fieldName, Object value) {
        if (source == null) {
            return;
        }
        Map<String, Field> fieldsMap = getFieldsMap(source);
        setValue(source, fieldsMap.get(fieldName), value);
    }


    /**
     * 获取指定名称字段的值 (有泛型).
     *
     * @param <T>       the type parameter
     * @param <V>       the type parameter
     * @param source    the source
     * @param fieldName the field name
     * @return the value
     */
    public static <T, V> V getValue(T source, String fieldName) {
        Map<String, Object> generateMap = generateMap(source);
        return (V) generateMap.get(fieldName);
    }

    /**
     * Gets value.
     *
     * @param source the source
     * @param field  the field
     * @return the value
     */
    public static Object getValue(Object source, Field field) {
        return getValue(source, field, Object.class);
    }

    /**
     * Gets value.
     *
     * @param <T>    the type parameter
     * @param <V>    the type parameter
     * @param source the source
     * @param field  the field
     * @param clazz  the clazz
     * @return the value
     */
    public static <T, V> V getValue(T source, Field field, Class<V> clazz) {
        boolean ok = field.isAccessible();
        QuickUtil.isFalse(ok, () -> field.setAccessible(true));
        Object o = ExceptionUtil.suppress(() -> field.get(source));
        QuickUtil.isFalse(ok, () -> field.setAccessible(false));
        return (V) o;
    }

    /**
     * Sets value.
     *
     * @param source the source
     * @param field  the field
     * @param value  the value
     */
    public static void setValue(Object source, Field field, Object value) {
        if (!Modifier.isFinal(field.getModifiers())) {
            boolean ok = field.isAccessible();
            QuickUtil.isFalse(ok, () -> field.setAccessible(true));
            ExceptionUtil.suppress(() -> field.set(source, value));
            QuickUtil.isFalse(ok, () -> field.setAccessible(false));
        }
    }


    /**
     * 获取指定包下的字节码.
     *
     * @param packs the packs
     * @return the classes
     */
    public static Set<Class<?>> getClasses(String... packs) {
        if (packs == null || packs.length == 0) {
            return Collections.emptySet();
        }
        Set<Class<?>> classes = new LinkedHashSet();
        for (String pack : packs) {
            classes.addAll(getClasses(pack, true));
        }
        return classes;
    }

    /**
     * 获取包下所有类.
     *
     * @param pack      the pack
     * @param recursive the recursive
     * @return classes classes
     */
    public static Set<Class<?>> getClasses(String pack, boolean recursive) {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet();
        // 获取包的名字 并进行替换
        String packDir = pack.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs = null;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packDir);
        } catch (IOException e) {
            ExceptionUtil.cast(String.format("加载文件夹异常: %s", packDir), e);
        }
        // 循环迭代下去
        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                fileClasses(recursive, classes, pack, url);
            } else if ("jar".equals(protocol)) {
                jarClasses(recursive, classes, pack, packDir, url);
            }
        }
        return classes;
    }

    private static void jarClasses(boolean recursive, Set<Class<?>> classes, String pack, String packDir, URL url) {
        try {
            // 获取jar
            JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
            // 从此jar包 得到一个枚举类
            Enumeration<JarEntry> entries = jar.entries();
            // 同样的进行循环迭代
            while (entries.hasMoreElements()) {
                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                // 如果是以/开头的
                if (name.charAt(0) == '/') {
                    // 获取后面的字符串
                    name = name.substring(1);
                }
                // 如果前半部分和定义的包名相同
                if (name.startsWith(packDir)) {
                    int idx = name.lastIndexOf('/');
                    String newPack = pack;
                    // 如果以"/"结尾 是一个包
                    if (idx != -1) {
                        // 获取包名 把"/"替换成"."
                        newPack = name.substring(0, idx).replace('/', '.');
                    }
                    // 如果可以迭代下去 并且是一个包
                    if ((idx != -1) || recursive) {
                        // 如果是一个.class文件 而且不是目录
                        if (name.endsWith(".class") && !entry.isDirectory()) {
                            // 去掉后面的".class" 获取真正的类名
                            String className = name.substring(newPack.length() + 1, name.length() - 6);
                            try {
                                // 添加到classes
                                classes.add(Class.forName(newPack + '.' + className));
                            } catch (NoClassDefFoundError e) {
                                log.warn("{} load fail, because {} not found.", newPack + '.' + className, e.getMessage());
                            } catch (ClassNotFoundException e) {
                                ExceptionUtil.cast(String.format("字节码丢失: %s", className), e);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            ExceptionUtil.cast(e);
        }
    }

    private static void fileClasses(boolean recursive, Set<Class<?>> classes, String pack, URL url) {
        try {
            // 获取包的物理路径
            String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
            // 以文件的方式扫描整个包下的文件 并添加到集合中
            fileClasses(pack, filePath, recursive, classes);
        } catch (UnsupportedEncodingException e) {
            ExceptionUtil.cast(String.format("字节码路径遇到编码异常: %s", url.getFile()), e);
        }
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param pack      the package name
     * @param packPath  the package path
     * @param recursive the recursive
     * @param classes   the classes
     */
    private static void fileClasses(String pack, String packPath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packPath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] files = dir.listFiles((File file) -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        // 循环所有文件
        for (File file : files) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                fileClasses(pack + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(pack + '.' + className));
                } catch (ClassNotFoundException e) {
                    ExceptionUtil.cast(String.format("字节码丢失: %s", className), e);
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * Get classes class [ ].
     *
     * @param os the os
     * @return the class [ ]
     */
    public static Class[] getClasses(Object... os) {
        if (BoolUtil.isEmpty(os)) {
            return new Class[0];
        }
        return Arrays.stream(os)
                .filter(Objects::nonNull)
                .map(Object::getClass).toArray(Class[]::new);
    }

    /**
     * 获取方法摘要.
     *
     * @param method the method
     * @return string 摘要: 默认对入参类型进行简单摘要 (即非全限定路径)
     */
    public static String getDigest(Method method) {
        // 默认对入参类型进行简单摘要 (即非全限定路径)
        return getDigest(method, Class::getSimpleName);
    }

    /**
     * 获取方法摘要.
     *
     * @param method the method
     * @param simple 是否对入参类型进行简单摘要 (即非全限定路径)
     * @return the digest
     */
    public static String getDigest(Method method, boolean simple) {
        return getDigest(method, simple ? Class::getSimpleName : Class::getName);
    }

    /**
     * 获取方法摘要.
     *
     * @param method 摘要方法
     * @param fun    获取入参类型的方法 (可选简单名称和全限定类名)
     * @return the digest
     */
    public static String getDigest(Method method, Function<Class, ? extends String> fun) {
        AssertUtil.notNull(method, "摘要的方法是空");
        Class<?> clazz = method.getDeclaringClass();
        String name = method.getName();
        Class<?>[] pts = method.getParameterTypes();
        String parameters = String
                .join(",", Arrays.stream(pts)
                        .filter(Objects::nonNull)
                        .map(fun).collect(Collectors.toList()));
        return String.format("%s#%s(%s)", clazz.getName(), name, parameters);
    }

    /**
     * Gets digest.
     *
     * @param methods the methods
     * @return the digest
     */
    public static Map<String, Method> getDigest(Method... methods) {
        if (BoolUtil.isEmpty(methods)) {
            return new HashMap<>();
        }
        return Arrays.stream(methods)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ClassUtil::getDigest, x -> x));
    }

    /**
     * Gets digest.
     *
     * @param simple  方法入参是否简签(非全限定类名)
     * @param methods the methods
     * @return the digest
     */
    public static Map<String, Method> getDigest(boolean simple, Method... methods) {
        if (BoolUtil.isEmpty(methods)) {
            return new HashMap<>();
        }
        return Arrays.stream(methods)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(m -> ClassUtil.getDigest(m, simple), x -> x));
    }

    /**
     * 根据方法摘要反射方法对象.
     * <p>可以不带(java.lang.String, int)字样</p>
     * <p>注意参数列表的类型是 simpleName 而非全限定路径 (参考示例) </p>
     *
     * @param method 示例: {@linkplain cn.gmlee.tools.base.util.HttpUtil#post(String, Kv[])}}
     * @return 方法对象 method
     */
    public static Method getMethod(String method) {
        AssertUtil.notEmpty(method, "方法摘要是空");
        AssertUtil.contain(method, "#", "方法摘要不合法");
        String clazz = method.substring(0, method.indexOf("#"));
        Class c = ExceptionUtil.sandbox(() -> Class.forName(clazz), false);
        AssertUtil.notNull(c, "方法摘要的字节码不存在");
        String m = method.substring(method.indexOf("#"));
        Map<String, Method> methodMap = m.contains(".") ? getDigest(false, c.getMethods()) : getDigest(c.getMethods());
        String key = method.replaceAll("\\s", "");
        return methodMap.get(key);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 方法调用.
     *
     * @param <R>    返回类型泛型
     * @param obj    静态方法传 null 即可
     * @param method 方法对象
     * @param args   方法入参
     * @return r 调用结果
     */
    public static <R> R call(Object obj, Method method, Object... args) {
        AssertUtil.notNull(method, "方法是空");
        return (R) ExceptionUtil.suppress(() -> method.invoke(obj, args));
    }

    /**
     * 调用方法.
     * <p>反射</p>
     *
     * @param <R>    返回类型泛型
     * @param obj    静态方法传 null 即可
     * @param method 示例: {@linkplain cn.gmlee.tools.base.util.LoginUtil#get(boolean)}}
     * @param args   方法入参
     * @return r 调用结果
     */
    public static <R> R call(Object obj, String method, Object... args) {
        Method m = getMethod(method);
        AssertUtil.notNull(m, "摘要的方法不存在");
        return (R) ExceptionUtil.suppress(() -> m.invoke(obj, args));
    }

    /**
     * 调用方法.
     *
     * @param <R>      返回类型泛型
     * @param obj      静态方法传 null 即可
     * @param method   方法摘要
     * @param jsonArgs 方法入参JSON字符串
     * @param names    参数名称 (请注意泛型擦除)
     * @return r 调用结果
     */
    public static <R> R callJsonArgs(Object obj, String method, String jsonArgs, String... names) {
        Method m = getMethod(method);
        AssertUtil.notNull(m, "摘要的方法不存在");
        Object[] args = getArgs(m, jsonArgs, names);
        return (R) ExceptionUtil.suppress(() -> m.invoke(obj, args));
    }

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * 简单指定方法名调用.
     *
     * <p>注意: 当前方法入参 {@param method} 不支持重载</p>
     *
     * @param <R>    返回类型泛型
     * @param obj    调用对象 (非空)
     * @param method 简单方法名(非摘要): callSimple、callSimpleJsonArgs
     * @param args   方法入参
     * @return r 返回对象
     */
    public static <R> R callSimple(Object obj, String method, Object... args) {
        Method m = getMethodMap(obj).get(method);
        AssertUtil.notNull(m, "方法不存在");
        return (R) ExceptionUtil.suppress(() -> m.invoke(obj, args));
    }

    /**
     * 简单指定方法名调用.
     *
     * <p>注意: 当前方法入参 {@param method} 不支持重载</p>
     *
     * @param <R>      返回类型泛型
     * @param obj      调用对象 (非空)
     * @param method   简单方法名(非摘要): callSimple、callSimpleJsonArgs
     * @param jsonArgs 方法入参JSON字符串
     * @param names    参数名称 (请注意泛型擦除)
     * @return r 返回对象
     */
    public static <R> R callSimpleJsonArgs(Object obj, String method, String jsonArgs, String... names) {
        Method m = getMethodMap(obj).get(method);
        AssertUtil.notNull(m, "方法不存在");
        Object[] args = getArgs(m, jsonArgs, names);
        return (R) ExceptionUtil.suppress(() -> m.invoke(obj, args));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Get args object [ ].
     *
     * @param method   the method
     * @param jsonArgs the json args
     * @param names    参数名称 (请注意泛型擦除)
     * @return the object [ ]
     */
    public static Object[] getArgs(Method method, String jsonArgs, String... names) {
        AssertUtil.notNull(method, "方法是空");
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 1) {
            return new Object[]{JsonUtil.toBean(jsonArgs, method.getParameterTypes()[0], true)};
        }
        Map map = JsonUtil.toBean(jsonArgs, Map.class);
        Object[] args = new Object[parameters.length];
        if (parameters.length < 1 || BoolUtil.isEmpty(map)) {
            // 没有参数 或者 参数是null
            return args;
        }
        AssertUtil.eq(names.length, parameters.length, "参数数量不符");
        for (int i = 0; i < args.length; i++) {
            Parameter p = parameters[i];
            Object o = map.get(names[i]);
            Object arg = o != null ? JsonUtil.convert(o, p.getType(), true) : null;
            args[i] = arg;
        }
        return args;
    }

    // -----------------------------------------------------------------------------------------------------------------


    /**
     * Gets method map.
     *
     * @param obj the obj
     * @return the method map
     */
    public static Map<String, Method> getMethodMap(Object obj) {
        return getMethodMap(obj, true);
    }

    /**
     * Gets method map.
     *
     * @param obj    the obj
     * @param simple the simple
     * @return the method map
     */
    public static Map<String, Method> getMethodMap(Object obj, boolean simple) {
        AssertUtil.notNull(obj, "对象是空");
        return getMethodMap(obj.getClass(), simple ? Method::getName : ClassUtil::getDigest);
    }

    /**
     * Gets method map.
     *
     * @param clazz the clazz
     * @param fun   the fun
     * @return the method map
     */
    public static Map<String, Method> getMethodMap(Class<?> clazz, Function<Method, String> fun) {
        AssertUtil.notNull(clazz, "字节码是空");
        Method[] methods = clazz.getMethods();
        if (BoolUtil.isEmpty(methods)) {
            return new HashMap<>();
        }
        return Arrays.stream(methods)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(fun, x -> x, (k, v) -> v));
    }

    /**
     * Gets method.
     *
     * @param clazz  the clazz
     * @param method 方法名(如: getKey、setVal)
     * @return the method
     */
    public static Method getMethod(Class<?> clazz, String method) {
        AssertUtil.notNull(clazz, "字节码是空");
        Method[] methods = clazz.getMethods();
        if (BoolUtil.isEmpty(methods)) {
            return null;
        }
        Optional<Method> optional = Arrays.stream(methods)
                .filter(Objects::nonNull)
                .filter(m -> m.getName().contains(method))
                .findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    /**
     * Gets digest.
     *
     * @param clazz  the clazz
     * @param method the method
     * @return the digest
     */
    public static String getDigest(Class<?> clazz, String method) {
        Method m = getMethod(clazz, method);
        AssertUtil.notNull(m, String.format("方法%s不存在于%s中", method, clazz.getName()));
        return getDigest(m);
    }

    /**
     * Gets digest.
     *
     * @param clazz  the clazz
     * @param method the method
     * @param simple 形参类型是否简单摘要 (是否全限定类名)
     * @return the digest
     */
    public static String getDigest(Class<?> clazz, String method, boolean simple) {
        Method m = getMethod(clazz, method);
        AssertUtil.notNull(m, String.format("方法%s不存在于%s中", method, clazz));
        return getDigest(m, simple);
    }

    // -----------------------------------------------------------------------------------------------------------------
}
