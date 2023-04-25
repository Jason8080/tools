package cn.gmlee.tools.webapp.assist;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * @author Jas°
 * @date 2020/11/4 (周三)
 */
public class StaticAssist {

    private static String separator;
    private static Boolean staticResourceMapEnable;
    private static String staticResourceMapDiskPath;
    private static String staticResourceMapClassPath;

    public static void init(String separator, Boolean staticResourceMapEnable, String staticResourceMapDiskPath, String staticResourceMapClassPath) {
        StaticAssist.separator = separator;
        StaticAssist.staticResourceMapEnable = staticResourceMapEnable;
        StaticAssist.staticResourceMapDiskPath = staticResourceMapDiskPath;
        StaticAssist.staticResourceMapClassPath = staticResourceMapClassPath;
    }

    public static void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (staticResourceMapEnable) {
            String filePath = getStaticResourceMapPath(staticResourceMapDiskPath);
            String classPath = getStaticResourceMapPath(staticResourceMapClassPath);
            registry.addResourceHandler("/disk/**")
                    .addResourceLocations(
                            "file:" + filePath,
                            "classpath:" + classPath
                    );
        }
    }

    private static String getStaticResourceMapPath(String path) {
        String str = path;
        if (!path.startsWith(separator)) {
            str = separator + path;
        }
        if (!path.endsWith(separator)) {
            str = path + separator;
        }
        return str;
    }
}
