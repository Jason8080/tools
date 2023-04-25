package cn.gmlee.tools.spring.util;

import cn.gmlee.tools.base.util.BoolUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * 资源文件加载工具类
 *
 * @author <a href="mailto:Jas@gm.com">Jas</a><br>
 */
public final class ResourceUtil {

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";

    private static boolean inited;
    private static boolean merged;

    private final static Properties allProperties = new Properties();

    private static Method envHelperGetPropertiesMethod;
    private static Method envHelperGetAllPropertiesMethod;

    private synchronized static void load() {
        if (inited) {
            return;
        }
        try {
            Class<?> threadClazz = Class.forName("cn.gmlee.tools.spring.helper.EnvironmentHelper");
            envHelperGetPropertiesMethod = threadClazz.getMethod("getProperty", String.class);
            envHelperGetAllPropertiesMethod = threadClazz.getMethod("getAllProperties", String.class);
        } catch (Exception e) {
        }

        merged = envHelperGetAllPropertiesMethod == null && envHelperGetPropertiesMethod == null;
        try {
            String classpath = System.getProperty("java.class.path");
            System.out.println("CLASSPATH: " + classpath);
            URL url = Thread.currentThread().getContextClassLoader().getResource("");
            if (url == null) {
                url = ResourceUtil.class.getResource("");
            }
            if (url == null) {
                return;
            }
            if ("file".equals(url.getProtocol())) {
                loadPropertiesFromFile(new File(url.getPath()));
            } else if (url.getProtocol().equals("jar")) {
                loadPropertiesFromJarFile(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inited = true;
        }
    }

    private static void loadPropertiesFromJarFile(URL url) throws UnsupportedEncodingException, IOException {

        System.out.println("loadPropertiesFromJarFile,origin:" + url.toString());
        String jarFilePath = url.getFile();
        if (jarFilePath.contains("war!")) {
            jarFilePath = jarFilePath.split("war!")[0] + "war";
        } else if (jarFilePath.contains("jar!")) {
            jarFilePath = jarFilePath.split("jar!")[0] + "jar";
        }
        jarFilePath = jarFilePath.substring("file:".length());
        jarFilePath = java.net.URLDecoder.decode(jarFilePath, "UTF-8");
        System.out.println("loadPropertiesFromJarFile,real:" + jarFilePath);
        JarFile jarFile = new JarFile(jarFilePath);

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".properties")) {
                InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(entry.getName()));
                Properties properties = new Properties();
                properties.load(inputStream);
                try {
                    inputStream.close();
                } catch (Exception e) {
                }

                allProperties.putAll(properties);
            }

        }
        jarFile.close();
    }

    private static void loadPropertiesFromFile(File parent) throws FileNotFoundException, IOException {
        File[] files = parent.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                loadPropertiesFromFile(file);
            } else {
                String path = file.getPath();
                if (path.endsWith("properties")) {
                    if (path.contains("i18n")) {
                        continue;
                    }
                    Properties p = new Properties();
                    p.load(new FileReader(file));

                    allProperties.putAll(p);
                    System.out.println("load properties from file:" + path);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    private synchronized static void mergeWithEnvironment() {
        if (merged) {
            return;
        }
        Map<String, Object> envProperties = null;
        if (envHelperGetAllPropertiesMethod != null) {
            try {
                envProperties = (Map<String, Object>) envHelperGetAllPropertiesMethod.invoke(null, "");
                if (envProperties == null || envProperties.isEmpty()) {
                    return;
                }
                for (String key : envProperties.keySet()) {
                    allProperties.setProperty(key, envProperties.get(key).toString());
                }
                merged = true;
            } catch (Exception e) {
            }
            return;
        }
        Set<Entry<Object, Object>> entrySet = allProperties.entrySet();

        for (Entry<Object, Object> entry : entrySet) {
            Object value = null;
            try {
                value = envHelperGetPropertiesMethod.invoke(null, entry.getKey());
                if (value != null) {
                    allProperties.setProperty(entry.getKey().toString(), value.toString());
                }
            } catch (Exception e) {
                return;
            }
        }

        merged = true;
    }

    /**
     * 获取所有配置的副本
     *
     * @return all properties
     */
    public static Properties getAllProperties() {
        return getAllProperties(null);
    }

    /**
     * 获取指定前缀的配置
     *
     * @param prefix the prefix
     * @return the all properties
     */
    public static Properties getAllProperties(String prefix) {
        if (!inited) {
            load();
        }
        if (!merged) {
            mergeWithEnvironment();
        }

        Properties properties = new Properties();
        Set<Entry<Object, Object>> entrySet = allProperties.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            if (BoolUtil.isBlank(prefix) || entry.getKey().toString().startsWith(prefix)) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        return properties;
    }

    /**
     * Get string.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the string
     */
    @Deprecated
    public static String get(String key, String... defaultValue) {
        if (defaultValue != null && defaultValue.length > 0 && defaultValue[0] != null) {
            return getProperty(key, defaultValue[0]);
        } else {
            return getProperty(key);
        }
    }

    /**
     * Gets property.
     *
     * @param key the key
     * @return the property
     */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Gets and validate property.
     *
     * @param key the key
     * @return the and validate property
     */
    public static String getAndValidateProperty(String key) {
        String value = getProperty(key, null);
        if (BoolUtil.isBlank(value)) {
            throw new IllegalArgumentException(String.format("Property for key:%s not exists", key));
        }
        return value;
    }

    /**
     * Gets property.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the property
     */
    public static String getProperty(String key, String defaultValue) {
        if (!inited) {
            load();
        }
        if (!merged) {
            mergeWithEnvironment();
        }

        //优先环境变量
        String value = System.getProperty(key);
        if (!BoolUtil.isBlank(value)) {
            return value;
        }

        value = System.getenv(key);
        if (!BoolUtil.isBlank(value)) {
            return value;
        }

        if (allProperties.containsKey(key)) {
            return allProperties.getProperty(key);
        }

        return defaultValue;
    }

    /**
     * Get int int.
     *
     * @param key the key
     * @return the int
     */
    public static int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Get int int.
     *
     * @param key          the key
     * @param defaultValue the defalut value
     * @return the int
     */
    public static int getInt(String key, int defaultValue) {
        String v = getProperty(key);
        if (v != null) {
            return Integer.parseInt(v);
        }
        return defaultValue;
    }

    /**
     * 获取int配置, 最小是least.
     *
     * @param key   the key
     * @param least the least
     * @return the least
     */
    public static int getLeast(String key, int least) {
        String v = getProperty(key);
        if (v != null) {
            int i = Integer.parseInt(v);
            return i < least ? least : i;
        }
        return least;
    }

    /**
     * Get long long.
     *
     * @param key the key
     * @return the long
     */
    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Get long long.
     *
     * @param key          the key
     * @param defaultValue the defalut value
     * @return the long
     */
    public static long getLong(String key, long defaultValue) {
        String v = getProperty(key);
        if (v != null) {
            return Long.parseLong(v);
        }
        return defaultValue;
    }

    /**
     * 获取long配置最小值是least.
     *
     * @param key   the key
     * @param least the least
     * @return the least
     */
    public static long getLeast(String key, long least) {
        String v = getProperty(key);
        if (v != null) {
            long l = Long.parseLong(v);
            return l < least ? least : l;
        }
        return least;
    }

    /**
     * Get boolean boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    /**
     * Merge.
     *
     * @param properties the properties
     */
    public synchronized static void merge(Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            Object value = null;
            try {
                value = envHelperGetPropertiesMethod.invoke(null, entry.getKey());
            } catch (Exception e) {
            }
            if (value == null) {
                value = entry.getValue();
            }
            if (value != null) {
                allProperties.setProperty(entry.getKey().toString(), value.toString());
            }
        }
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    public synchronized static void add(String key, String value) {
        if (BoolUtil.isBlank(key, value)) {
            return;
        }
        addToProperties(key, value);
    }

    /**
     * Contains property boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public static boolean containsProperty(String key) {
        return allProperties.containsKey(key);
    }

    /**
     * 如果替换包含占位符则替换占位符
     *
     * @param key
     * @return
     */
    private static String addToProperties(String key, String value) {

        if (!value.contains(PLACEHOLDER_PREFIX)) {
            allProperties.put(key, value);
            return value;
        }

        String[] segments = value.split("\\$\\{");
        String seg;

        StringBuilder finalValue = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            seg = segments[i] == null? "" : segments[i];
            if (BoolUtil.isBlank(seg)) {
                continue;
            }

            if (seg.contains(PLACEHOLDER_SUFFIX)) {
                String refKey = seg.substring(0, seg.indexOf(PLACEHOLDER_SUFFIX)).trim();
                //其他非${}的占位符如：{{host}}
                String withBraceString = null;
                if (seg.contains("{")) {
                    withBraceString = seg.substring(seg.indexOf(PLACEHOLDER_SUFFIX) + 1);
                }

                //如果包含默认值，如：${host:127.0.0.1}
                String defaultValue = null;
                if (refKey.contains(":")) {
                    String[] tmpArray = refKey.split(":");
                    refKey = tmpArray[0];
                    defaultValue = tmpArray[1];
                }

                String refValue = getProperty(refKey);
                if (BoolUtil.isBlank(refValue)) {
                    refValue = defaultValue;
                }
                finalValue.append(refValue);

                if (withBraceString != null) {
                    finalValue.append(withBraceString);
                } else {
                    String[] segments2 = seg.split("\\}");
                    if (segments2.length == 2) {
                        finalValue.append(segments2[1]);
                    }
                }
            } else {
                finalValue.append(seg);
            }
        }

        allProperties.put(key, finalValue.toString());

        return finalValue.toString();
    }

}
