package cn.gmlee.tools.swagger.config;

import cn.gmlee.tools.swagger.assist.SwaggerAssist;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 兼容SpringBoot低版本.
 *
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
@AutoConfigureAfter(SwaggerWebMvcAutoConfiguration.class)
@ConditionalOnMissingBean(SwaggerWebMvcAutoConfiguration.class)
public class SwaggerWebMvcConfigurerAdapter extends WebMvcConfigurerAdapter {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        // 注册Swagger静态资源
        SwaggerAssist.addResourceHandler(registry);
    }
}
