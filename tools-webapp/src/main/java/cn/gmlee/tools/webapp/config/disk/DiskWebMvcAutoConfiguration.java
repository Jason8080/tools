package cn.gmlee.tools.webapp.config.disk;

import cn.gmlee.tools.webapp.assist.StaticAssist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
public class DiskWebMvcAutoConfiguration implements WebMvcConfigurer {
    protected String separator = "/";
    @Value("${tools.webapp.staticResourceMapEnable:false}")
    private Boolean staticResourceMapEnable;
    @Value("${tools.webapp.staticResourceMapDiskPath:}")
    private String staticResourceMapDiskPath;
    @Value("${tools.webapp.staticResourceMapClassPath:}")
    private String staticResourceMapClassPath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注册Disk静态文件映射
        StaticAssist.init(separator, staticResourceMapEnable, staticResourceMapDiskPath, staticResourceMapClassPath);
        StaticAssist.addResourceHandlers(registry);
    }
}
