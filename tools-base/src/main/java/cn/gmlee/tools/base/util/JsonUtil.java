package cn.gmlee.tools.base.util;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.jackson.JacksonAssist;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.util.StringUtils;

import java.util.TimeZone;

/**
 * JSON 转换工具
 *
 * @author Jas °
 * @date 2020 /8/28 (周五)
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper = newInstance();
    private static final ObjectMapper objectMapperIncludeAlways = newInstance();
    private static final ObjectMapper objectMapperSnakeCase = newInstance();

    static {
        objectMapperIncludeAlways.setSerializationInclusion(Include.ALWAYS);
        objectMapperSnakeCase.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    /**
     * Jackson .
     *
     * @return the object mapper
     */
    private static ObjectMapper newInstance() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 默认注册: 保持与框架同步
        JacksonAssist.registerDefaultModule(objectMapper);
        // 默认转换: 默认long类型会转换成String
        JacksonAssist.registerTypeModule(objectMapper, true);
        // 默认时区: 默认 GMT+8 时区 yyyy-MM-dd HH:mm:ss 格式
        JacksonAssist.registerTimeZoneModule(objectMapper, TimeZone.getTimeZone("GMT+8"), XTime.SECOND_MINUS_BLANK_COLON.pattern);
        return objectMapper;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    /**
     * To json string.
     *
     * @param obj the obj
     * @return the string
     * @throws Exception the exception
     */
    public static String toJson(Object obj) {
        return toJson(obj, false, false);
    }

    /**
     * To json string.
     *
     * @param obj        the obj
     * @param allowEx    the allowEx
     * @param ignoreNull the ignoreNull
     * @return the string
     */
    public static String toJson(Object obj, boolean allowEx, boolean ignoreNull) {
        if (obj == null) {
            return allowEx ? null : "";
        }
        ObjectMapper objectMapper = getInstance();
        if (!ignoreNull) {
            objectMapper = objectMapperIncludeAlways;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * To bean t.
     * <p>
     * 优化jackson操作api, 支持用jdk的可变参数无限套娃.
     * 注意: 如果一个class中存在多个范型请绕行 -> {@link #toBean(String, JavaType)}
     * {@see com.fasterxml.jackson.databind.type.TypeFactory#constructParametricType(Class, Class) // 一个class多个范型的原生api}
     * </p>
     *
     * @param <T>     the type parameter
     * @param json    the json
     * @param classes 请注意此处的范型是1层1层往里面嵌套的.
     * @return the t
     */
    public static <T> T toBean(String json, Class<?>... classes) {
        if (BoolUtil.isEmpty(classes)) {
            return (T) toBean(json, Object.class, false);
        }
        if (classes.length == 1) {
            return (T) toBean(json, classes[0], false);
        }
        JavaType javaType = getInstance().getTypeFactory().constructType(classes[classes.length - 1]);
        for (int i = classes.length - 1 - 1; i >= 0; i--) {
            javaType = getInstance().getTypeFactory().constructParametricType(classes[i], javaType);
        }
        return toBean(json, javaType, false);
    }

    /**
     * To bean t.
     *
     * @param <T>      the type parameter
     * @param json     the json
     * @param javaType the java type
     * @return the t
     */
    public static <T> T toBean(String json, JavaType javaType) {
        return toBean(json, javaType, false);
    }

    /**
     * To bean t.
     *
     * @param <T>       the type parameter
     * @param json      the json
     * @param reference the reference
     * @return the t
     */
    public static <T> T toBean(String json, TypeReference<T> reference) {
        return toBean(json, reference, false);
    }

    /**
     * To bean t.
     *
     * @param <T>     the type parameter
     * @param json    the json
     * @param clazz   the clazz
     * @param allowEx the allow ex
     * @return the t
     */
    public static <T> T toBean(String json, Class<T> clazz, boolean allowEx) {
        if (json == null) {
            return null;
        }
        try {
            return getInstance().readValue(json, clazz);
        } catch (Exception e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * To bean t.
     *
     * @param <T>      the type parameter
     * @param json     the json
     * @param javaType the java type
     * @param allowEx  the allowEx
     * @return the t
     */
    public static <T> T toBean(String json, JavaType javaType, boolean allowEx) {
        if (json == null) {
            return null;
        }
        try {
            return getInstance().readValue(json, javaType);
        } catch (Exception e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * To bean t.
     *
     * @param <T>       the type parameter
     * @param json      the json
     * @param reference the reference
     * @param allowEx   the allowEx
     * @return the t
     */
    public static <T> T toBean(String json, TypeReference<T> reference, boolean allowEx) {
        if (json == null) {
            return null;
        }
        try {
            return getInstance().readValue(json, reference);
        } catch (Exception e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * Convert t.
     *
     * @param <T>   the type parameter
     * @param obj   the obj
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T convert(Object obj, Class<T> clazz) {
        return convert(obj, clazz, false);
    }


    /**
     * Convert t.
     *
     * @param <T>      the type parameter
     * @param obj      the obj
     * @param clazz    the clazz
     * @param allowEx  the allowEx
     * @param useSnake 使用反驼峰命名法(适用DB)
     * @return the t
     */
    public static <T> T convert(Object obj, Class<T> clazz, boolean allowEx, boolean useSnake) {
        ObjectMapper objectMapper = getInstance();
        // 开启驼峰命名: json串中的hoMe 将 将无法再转到 属性中
        if (useSnake) {
            objectMapper = objectMapperSnakeCase;
        }
        try {
            return objectMapper.convertValue(obj, clazz);
        } catch (IllegalArgumentException e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * Convert t.
     *
     * @param <T>     the type parameter
     * @param obj     the obj
     * @param clazz   the clazz
     * @param allowEx the allow ex
     * @return the t
     */
    public static <T> T convert(Object obj, Class<T> clazz, boolean allowEx) {
        try {
            return getInstance().convertValue(obj, clazz);
        } catch (IllegalArgumentException e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * 转换成java类型的对象.
     * <p>
     * 似乎需要@class
     * </p>
     *
     * @param <T>      the type parameter
     * @param obj      the obj
     * @param javaType the java type
     * @param allowEx  the allow ex
     * @return the t
     */
    public static <T> T convert(Object obj, JavaType javaType, boolean allowEx) {
        try {
            return getInstance().convertValue(obj, javaType);
        } catch (IllegalArgumentException e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }


    /**
     * Convert t.
     *
     * @param <T>       the type parameter
     * @param obj       the obj
     * @param reference the reference
     * @param allowEx   the allow ex
     * @return the t
     */
    public static <T> T convert(Object obj, TypeReference<T> reference, boolean allowEx) {
        try {
            return getInstance().convertValue(obj, reference);
        } catch (IllegalArgumentException e) {
            if (allowEx) {
                return ExceptionUtil.cast(e);
            }
        }
        return null;
    }

    /**
     * Convert t.
     *
     * @param <T>       the type parameter
     * @param obj       the obj
     * @param reference the reference
     * @return the t
     */
    public static <T> T convert(Object obj, TypeReference<T> reference) {
        return convert(obj, reference, false);
    }

    /**
     * Convert t.
     *
     * @param <T>      the type parameter
     * @param obj      the obj
     * @param javaType the java type
     * @return the t
     */
    public static <T> T convert(Object obj, JavaType javaType) {
        return convert(obj, javaType, false);
    }

    /**
     * To bytes byte [ ].
     *
     * @param params the params
     * @return the byte [ ]
     */
    public static byte[] toBytes(Object params) {
        String json = toJson(params);
        if (!StringUtils.isEmpty(json)) {
            return json.getBytes();
        }
        return null;
    }

    /**
     * 将对象的json字符串格式化输出.
     *
     * @param obj the obj
     * @return the string
     */
    public static String format(Object obj) {
        String json = toJson(obj);
        return format(json);
    }

    /**
     * 对json字符串格式化输出
     *
     * @param json the json str
     * @return string string
     */
    public static String format(String json) {
        if (StringUtils.isEmpty(json)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        char last, current = '\0';
        int indent = 0;
        boolean entire = true;
        for (int i = 0; i < json.length(); i++) {
            last = current;
            current = json.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    space(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    space(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && entire) {
                        sb.append('\n');
                        space(sb, indent);
                    }
                    break;
                case '\"':
                    sb.append(current);
                    entire = !entire;
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }


    /**
     * 添加space
     *
     * @param sb
     * @param indent
     */
    private static void space(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }
}
