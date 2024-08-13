package cn.gmlee.tools.base.jackson;

import cn.gmlee.tools.base.define.Codec;
import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * The type Jackson assist.
 *
 * @author Jas °
 * @date 2020 /11/4 (周三)
 */
public class JacksonAssist {
    /**
     * 注册默认模块.
     *
     * @param objectMapper the object mapper
     */
    public static void registerDefaultModule(ObjectMapper objectMapper) {
        // 注册时间
        JacksonAssist.registerTimeModule(objectMapper);
        // 注册禁用
        JacksonAssist.registerDisableModule(objectMapper);
        // 注册启用
        JacksonAssist.registerEnableModule(objectMapper);
        // 注册忽略
        JacksonAssist.registerIgnoreModule(objectMapper);
        // 注册允许
        JacksonAssist.registerAllowModule(objectMapper);
        // 注册接收
        JacksonAssist.registerAcceptModule(objectMapper);
    }

    /**
     * 注册时间模块
     *
     * @param objectMapper the object mapper
     */
    public static void registerTimeModule(ObjectMapper objectMapper) {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule timeModule = new SimpleModule();
        // 默认值
        objectMapper.getSerializationConfig().with(XTime.SECOND_MINUS_BLANK_COLON.dateFormat);
        objectMapper.getDeserializationConfig().with(XTime.SECOND_MINUS_BLANK_COLON.dateFormat);
        // 序列化
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(Date.class, DateSerializer.instance);
        // 反序列
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(Date.class, TimeJsonDeserializer.instance);
        objectMapper.registerModule(timeModule);
    }

    /**
     * 注册禁用模块
     *
     * @param objectMapper the object mapper
     */
    public static void registerDisableModule(ObjectMapper objectMapper) {
        // 序列化时: 空对象不抛出异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 序列化时: 对象少了属性不抛出异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 注册允许模块
     *
     * @param objectMapper the object mapper
     */
    public static void registerAllowModule(ObjectMapper objectMapper) {
        // 允许属性名称没有引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    /**
     * 注册接收模块.
     *
     * @param objectMapper the object mapper
     */
    public static void registerAcceptModule(ObjectMapper objectMapper) {
        // 接受空对象: 空符
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        // 接受简单值: 数组
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }


    /**
     * 注册忽略模块
     *
     * @param objectMapper the object mapper
     */
    public static void registerIgnoreModule(ObjectMapper objectMapper) {
        // 忽略  null 的属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 忽略 transient 修饰的属性
        objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }


    /**
     * 注册启用模块
     *
     * @param objectMapper the object mapper
     */
    public static void registerEnableModule(ObjectMapper objectMapper) {
        // 开启 json 格式化
//        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 开启 json 有序性
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    /**
     * Register type module.
     *
     * @param objectMapper the object mapper
     * @param longToString the long to string
     */
    public static void registerTypeModule(ObjectMapper objectMapper, Boolean longToString) {
        // 构建模块
        SimpleModule typeModule = new SimpleModule();
        // 精度丢失
        QuickUtil.isTrue(longToString || longToString == null, () -> typeModule.addSerializer(Long.class, ToStringSerializer.instance));
        // 注册模块
        objectMapper.registerModule(typeModule);
    }

    /**
     * 注册编解码模块
     *
     * @param objectMapper
     */
    public static void registerCodecModule(ObjectMapper objectMapper, Codec... codecs) {
        if(BoolUtil.isEmpty(codecs)){
            return;
        }
        // 构建模块
        SimpleModule codecModule = new SimpleModule();
        for (Codec codec : codecs) {
            if(codec instanceof JsonSerializer){
                codecModule.addSerializer(codec.support(), (JsonSerializer) codec);
            }
            if(codec instanceof JsonDeserializer){
                codecModule.addDeserializer(codec.support(), (JsonDeserializer) codec);
            }
        }
        // 注册模块
        objectMapper.registerModule(codecModule);
    }

    /**
     * Register time zone module.
     *
     * @param objectMapper the object mapper
     * @param timeZone     the time zone
     * @param dateFormat   the date format
     */
    public static void registerTimeZoneModule(ObjectMapper objectMapper, TimeZone timeZone, String dateFormat) {
        // 注入时区
        QuickUtil.notNull(timeZone, x -> objectMapper.setTimeZone(x));
        // 注入时间格式: 默认yyyy-MM-dd HH:mm:ss
        QuickUtil.is(BoolUtil.notEmpty(dateFormat),
                () -> objectMapper.setDateFormat(new SimpleDateFormat(dateFormat)),
                () -> objectMapper.setDateFormat(XTime.SECOND_MINUS_BLANK_COLON.dateFormat));
    }
}
