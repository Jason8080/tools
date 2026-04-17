package cn.gmlee.tools.jackson.config;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.BoolUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
@ConditionalOnClass(WebMvcConfigurer.class)
@AutoConfiguration(after = JacksonAutoConfiguration.class)
@EnableConfigurationProperties({JacksonModuleProperties.class, JacksonProperties.class})
public class JacksonWebMvcConfigurer implements WebMvcConfigurer {

    @Resource
    private JacksonProperties jacksonProperties;

    @Resource
    private JacksonModuleProperties jacksonModuleProperties;

    @Bean
    public JsonMapperBuilderCustomizer toolsJacksonJsonMapperBuilderCustomizer() {
        return builder -> {
            // 与历史行为对齐
            builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
            builder.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            builder.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

            // 时间配置：优先 Spring Boot 的 jackson.* 配置，兜底到历史默认格式
            if (jacksonProperties.getTimeZone() != null) {
                builder.defaultTimeZone(jacksonProperties.getTimeZone());
            }
            if (jacksonProperties.getDateFormat() != null && !jacksonProperties.getDateFormat().isEmpty()) {
                builder.defaultDateFormat(new SimpleDateFormat(jacksonProperties.getDateFormat()));
            }

            DateTimeFormatter dateTimeFormatter = BoolUtil.isEmpty(jacksonProperties.getDateFormat())
                    ? XTime.SECOND_MINUS_BLANK_COLON.timeFormat
                    : DateTimeFormatter.ofPattern(jacksonProperties.getDateFormat());

            // 覆盖 JavaTime 默认 ISO 输出（否则会出现 2026-04-17T09:14:24.79387）
            SimpleModule timeModule = new SimpleModule();
            timeModule.addSerializer(java.time.LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
            timeModule.addSerializer(java.time.LocalDate.class, new LocalDateSerializer(dateTimeFormatter));
            timeModule.addSerializer(java.time.LocalTime.class, new LocalTimeSerializer(dateTimeFormatter));
            timeModule.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
            timeModule.addDeserializer(java.time.LocalDate.class, new LocalDateDeserializer(dateTimeFormatter));
            timeModule.addDeserializer(java.time.LocalTime.class, new LocalTimeDeserializer(dateTimeFormatter));
            builder.addModule(timeModule);

            // Long 转字符串，避免前端精度丢失
            if (jacksonModuleProperties.getLongToString() == null || jacksonModuleProperties.getLongToString()) {
                SimpleModule typeModule = new SimpleModule();
                typeModule.addSerializer(Long.class, ToStringSerializer.instance);
                builder.addModule(typeModule);
            }
        };
    }
}
