package cn.gmlee.tools.jackson.config;

import cn.gmlee.tools.base.jackson.JacksonAssist;
import cn.gmlee.tools.base.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.text.SimpleDateFormat;

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

    @PostConstruct
    public void initJsonUtilMapper() {
        // 保持 JsonUtil 的 ObjectMapper 与 Spring 配置对齐（兼容老工具调用链）
        autoInjectSpringOriginal(JsonUtil.getInstance(), jacksonProperties, jacksonModuleProperties);
        JacksonAssist.registerDefaultModule(JsonUtil.getInstance());
    }

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

            // Long 转字符串，避免前端精度丢失
            if (jacksonModuleProperties.getLongToString() == null || jacksonModuleProperties.getLongToString()) {
                SimpleModule typeModule = new SimpleModule();
                typeModule.addSerializer(Long.class, ToStringSerializer.instance);
                builder.addModule(typeModule);
            }
        };
    }

    private static void autoInjectSpringOriginal(ObjectMapper objectMapper, JacksonProperties jacksonProperties, JacksonModuleProperties jacksonModuleProperties) {
        // 类型转换
        JacksonAssist.registerTypeModule(objectMapper, jacksonModuleProperties.getLongToString());
        // 时区转换
        JacksonAssist.registerTimeZoneModule(objectMapper, jacksonProperties.getTimeZone(), jacksonProperties.getDateFormat());
    }
}
