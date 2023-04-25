package cn.gmlee.tools.jackson.config;

import cn.gmlee.tools.base.enums.XTime;
import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.QuickUtil;
import cn.gmlee.tools.jackson.assist.JacksonAssist;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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
                autoInjectSpringOriginal(objectMapper, jacksonProperties);
                JacksonAssist.registerAllModule(objectMapper, jacksonModuleProperties);
            }
        });
    }


    private static void autoInjectSpringOriginal(ObjectMapper objectMapper, JacksonProperties jacksonProperties) {
        // 注入时区
//        QuickUtil.is(BoolUtil.notNull(jacksonProperties.getTimeZone()),
//                () -> objectMapper.setTimeZone(jacksonProperties.getTimeZone()),
//                () -> objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8")));
        QuickUtil.notNull(jacksonProperties.getTimeZone(), x -> objectMapper.setTimeZone(x));
        // 注入时间格式: 默认yyyy-MM-dd HH:mm:ss.SSS
        QuickUtil.is(BoolUtil.notEmpty(jacksonProperties.getDateFormat()),
                () -> objectMapper.setDateFormat(new SimpleDateFormat(jacksonProperties.getDateFormat())),
                () -> objectMapper.setDateFormat(XTime.MS_MINUS_BLANK_COLON_DOT.dateFormat));
    }
}
