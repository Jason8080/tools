package cn.gmlee.tools.jackson.assist;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.jackson.config.JacksonModuleProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * The type Jackson assist.
 *
 * @author Jas °
 * @date 2020 /11/4 (周三)
 */
public class JacksonAssist {
    /**
     * 注册所有模块.
     *
     * @param objectMapper the object mapper
     * @param module       the module
     */
    public static void registerAllModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        // 注册时间类型转换模块
        JacksonAssist.registerTimeModule(objectMapper, module);
        // 注册其他类型转换模块
        JacksonAssist.registerConvertModule(objectMapper, module);
        // 注册异常禁止抛出模块
        JacksonAssist.registerDisableModule(objectMapper, module);
        // 注册常见情况允许模块
        JacksonAssist.registerAllowModule(objectMapper, module);
        // 注册特定忽略场景模块
        JacksonAssist.registerIgnoreModule(objectMapper, module);
        // 注册开启特殊功能模块
        JacksonAssist.registerEnableModule(objectMapper, module);
    }

    /**
     * 注册时间模块
     *
     * @param objectMapper the object mapper
     * @param module       the module
     */
    @SuppressWarnings("all")
    public static void registerTimeModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule timeModule = new SimpleModule();
        // 序列化
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addSerializer(Date.class, DateSerializer.instance);
        // 反序列
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(XTime.SECOND_MINUS_BLANK_COLON.timeFormat));
        timeModule.addDeserializer(Date.class, DateDeserializers.DateDeserializer.instance);
        objectMapper.registerModule(timeModule);
    }

    /**
     * 注册转换模块
     *
     * @param objectMapper the object mapper
     * @param module       the module
     */
    public static void registerConvertModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        SimpleModule typeModule = new SimpleModule();
        // Long类型都以String方式返回: 解决精度丢失问题
        QuickUtil.isTrue(module.getLongToString(), () -> typeModule.addSerializer(Long.class, ToStringSerializer.instance));
        objectMapper.registerModule(typeModule);
    }


    /**
     * 注册禁用模块
     *
     * @param objectMapper the object mapper
     * @param module       the module
     */
    public static void registerDisableModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        // 序列化时: 空对象不抛出异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 序列化时: 对象少了属性不抛出异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 注册允许模块
     *
     * @param objectMapper
     * @param module
     */
    public static void registerAllowModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        // 允许属性名称没有引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }


    /**
     * 注册忽略模块
     *
     * @param objectMapper the object mapper
     * @param module       the module
     */
    public static void registerIgnoreModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        // 忽略  null 的属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 忽略 transient 修饰的属性
        objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
    }


    /**
     * 注册启用模块
     *
     * @param objectMapper the object mapper
     * @param module       the module
     */
    public static void registerEnableModule(ObjectMapper objectMapper, JacksonModuleProperties module) {
        // 开启 json 格式化
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 开启 json 有序性
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }
}
