package cn.gmlee.tools.swagger.config;

import cn.gmlee.tools.swagger.assist.SwaggerAssist;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
public class SwaggerWebMvcAutoConfiguration implements WebMvcConfigurer {
    /**
     * 发现如果继承了WebMvcConfigurationSupport，则在yml中配置的相关内容会失效。 需要重新指定静态资源
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注册Swagger静态资源
        SwaggerAssist.addResourceHandler(registry);
    }
}
