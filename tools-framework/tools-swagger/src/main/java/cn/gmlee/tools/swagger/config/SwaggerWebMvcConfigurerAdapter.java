package cn.gmlee.tools.swagger.config;

import cn.gmlee.tools.swagger.assist.SwaggerAssist;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 兼容：在缺失 {@link SwaggerWebMvcAutoConfiguration} 时补充静态资源映射.
 *
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
@Configuration
@AutoConfigureAfter(SwaggerWebMvcAutoConfiguration.class)
@ConditionalOnMissingBean(SwaggerWebMvcAutoConfiguration.class)
public class SwaggerWebMvcConfigurerAdapter implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        SwaggerAssist.addResourceHandler(registry);
    }
}
