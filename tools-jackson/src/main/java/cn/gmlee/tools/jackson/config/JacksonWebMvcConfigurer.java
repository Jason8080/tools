package cn.gmlee.tools.jackson.config;

import cn.gmlee.tools.base.jackson.JacksonAssist;
import cn.gmlee.tools.base.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
@ConditionalOnClass(WebMvcConfigurer.class)
@EnableConfigurationProperties({JacksonModuleProperties.class, JacksonProperties.class})
public class JacksonWebMvcConfigurer implements WebMvcConfigurer {

    @Resource
    private JacksonProperties jacksonProperties;

    @Resource
    private JacksonModuleProperties jacksonModuleProperties;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.forEach(x -> {
            if (x instanceof AbstractJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ((AbstractJackson2HttpMessageConverter) x).getObjectMapper();
                // 注入框架原生配置
                autoInjectSpringOriginal(objectMapper, jacksonProperties, jacksonModuleProperties);
                // 注册其他默认配置
                JacksonAssist.registerDefaultModule(objectMapper);
            }
        });
    }


    private static void autoInjectSpringOriginal(ObjectMapper objectMapper, JacksonProperties jacksonProperties, JacksonModuleProperties jacksonModuleProperties) {
        // 类型转换
        JacksonAssist.registerTypeModule(objectMapper, jacksonModuleProperties.getLongToString());
        JacksonAssist.registerTypeModule(JsonUtil.getInstance(), jacksonModuleProperties.getLongToString());
        // 时区转换
        JacksonAssist.registerTimeZoneModule(objectMapper, jacksonProperties.getTimeZone(), jacksonProperties.getDateFormat());
        JacksonAssist.registerTimeZoneModule(JsonUtil.getInstance(), jacksonProperties.getTimeZone(), jacksonProperties.getDateFormat());
    }
}
