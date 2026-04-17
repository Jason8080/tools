package cn.gmlee.tools.base.jackson;

import cn.gmlee.tools.base.define.Codec;
import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonFactoryBuilder;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.jdk.JavaUtilDateSerializer;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * The type Jackson assist.
 *
 * @author Jas °
 * @date 2020 /11/4 (周三)
 */
public class JacksonAssist {
    private static ObjectMapper rebuild(ObjectMapper objectMapper, Consumer<MapperBuilder<?, ?>> customizer) {
        MapperBuilder<?, ?> builder = objectMapper.rebuild();
        customizer.accept(builder);
        return builder.build();
    }

    /**
     * 注册默认模块.
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerDefaultModule(ObjectMapper objectMapper) {
        // 先处理工厂级特性，再处理 mapper 级配置
        objectMapper = JacksonAssist.registerAllowModule(objectMapper);
        // 注册时间
        objectMapper = JacksonAssist.registerTimeModule(objectMapper);
        // 注册禁用
        objectMapper = JacksonAssist.registerDisableModule(objectMapper);
        // 注册启用
        objectMapper = JacksonAssist.registerEnableModule(objectMapper);
        // 注册忽略
        objectMapper = JacksonAssist.registerIgnoreModule(objectMapper);
        // 注册接收
        return JacksonAssist.registerAcceptModule(objectMapper);
    }

    /**
     * 注册时间模块
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerTimeModule(ObjectMapper objectMapper) {
        SimpleModule timeModule = new SimpleModule();
        // 序列化
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(Date.class, JavaUtilDateSerializer.instance);
        // 反序列
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(Date.class, TimeJsonDeserializer.instance);
        return rebuild(objectMapper, builder -> {
            builder.addModule(timeModule);
            builder.defaultDateFormat(XTime.SECOND_MINUS_BLANK_COLON.dateFormat);
        });
    }

    /**
     * 注册禁用模块
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerDisableModule(ObjectMapper objectMapper) {
        // 序列化时: 空对象不抛出异常
        return rebuild(objectMapper, builder -> {
            builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            // 反序列化时: 对象少了属性不抛出异常
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        });
    }

    /**
     * 注册允许模块
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerAllowModule(ObjectMapper objectMapper) {
        if (!(objectMapper.tokenStreamFactory() instanceof JsonFactory)) {
            return objectMapper;
        }
        JsonFactory factory = (JsonFactory) objectMapper.tokenStreamFactory();
        JsonFactoryBuilder builder = factory.rebuild();
        // 允许属性名称没有引号 + 允许单引号
        builder.enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES);
        builder.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES);
        return new ObjectMapper(builder.build());
    }

    /**
     * 注册接收模块.
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerAcceptModule(ObjectMapper objectMapper) {
        // 接受空对象: 空符
        return rebuild(objectMapper, builder -> {
            builder.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            // 接受简单值: 数组
            builder.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        });
    }


    /**
     * 注册忽略模块
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerIgnoreModule(ObjectMapper objectMapper) {
        return rebuild(objectMapper, builder -> {
            // 忽略 null 的属性
            builder.changeDefaultPropertyInclusion(old -> JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));
            // 忽略 transient 修饰的属性
            builder.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        });
    }


    /**
     * 注册启用模块
     *
     * @param objectMapper the object mapper
     */
    public static ObjectMapper registerEnableModule(ObjectMapper objectMapper) {
        // 开启 json 格式化
//        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 开启 json 有序性
        return rebuild(objectMapper, builder -> {
            builder.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
            builder.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        });
    }

    /**
     * Register type module.
     *
     * @param objectMapper the object mapper
     * @param longToString the long to string
     */
    public static ObjectMapper registerTypeModule(ObjectMapper objectMapper, Boolean longToString) {
        // 构建模块
        SimpleModule typeModule = new SimpleModule();
        // 精度丢失
        QuickUtil.isTrue(longToString || longToString == null, () -> typeModule.addSerializer(Long.class, ToStringSerializer.instance));
        // 注册模块
        return rebuild(objectMapper, builder -> builder.addModule(typeModule));
    }

    /**
     * 注册编解码模块
     *
     * @param objectMapper
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ObjectMapper registerCodecModule(ObjectMapper objectMapper, Codec... codecs) {
        if(BoolUtil.isEmpty(codecs)){
            return objectMapper;
        }
        // 构建模块
        SimpleModule codecModule = new SimpleModule();
        for (Codec codec : codecs) {
            if(codec instanceof ValueSerializer){
                codecModule.addSerializer(codec.support(), (ValueSerializer) codec);
            }
            if(codec instanceof ValueDeserializer){
                codecModule.addDeserializer(codec.support(), (ValueDeserializer) codec);
            }
        }
        // 注册模块
        return rebuild(objectMapper, builder -> builder.addModule(codecModule));
    }

    /**
     * Register time zone module.
     *
     * @param objectMapper the object mapper
     * @param timeZone     the time zone
     * @param dateFormat   the date format
     */
    public static ObjectMapper registerTimeZoneModule(ObjectMapper objectMapper, TimeZone timeZone, String dateFormat) {
        return rebuild(objectMapper, builder -> {
            // 注入时区
            QuickUtil.notNull(timeZone, builder::defaultTimeZone);
            // 注入时间格式: 默认yyyy-MM-dd HH:mm:ss
            QuickUtil.is(BoolUtil.notEmpty(dateFormat),
                    () -> builder.defaultDateFormat(new SimpleDateFormat(dateFormat)),
                    () -> builder.defaultDateFormat(XTime.SECOND_MINUS_BLANK_COLON.dateFormat));
        });
    }
}
