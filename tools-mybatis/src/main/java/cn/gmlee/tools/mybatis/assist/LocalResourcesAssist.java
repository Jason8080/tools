package cn.gmlee.tools.mybatis.assist;

import cn.gmlee.tools.base.util.CollectionUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * The type Local resources assist.
 *
 * @author Jas °
 * @date 2020 /11/5 (周四)
 */
public class LocalResourcesAssist {

    /**
     * Get packages resource resource [ ].
     *
     * @param packages the packages
     * @return the resource [ ]
     * @throws IOException the io exception
     */
    public static Resource[] getPackagesResource(String... packages) throws IOException {
        Resource[] all = {};
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (String packageName : packages){
            Resource[] resources = resolver.getResources("classpath*:/**/" + packageName + "/**/*.xml");
            all = CollectionUtil.merge(all, resources);
        }
        return all;
    }

    /**
     * 只读取项目本地的File
     * <p>
     * UrlResource将被排除
     * </p>
     *
     * @param resources the resources
     * @return resource [ ]
     */
    public static Resource[] getFileSystemResource(Resource... resources) {
        if (resources != null && resources.length > 0) {
            return Arrays
                    .stream(resources)
                    .filter(x -> {
                        if (x instanceof FileSystemResource) {
                            return true;
                        } else if (x instanceof UrlResource) {
                            URL url = ((UrlResource) x).getURL();
                            if (url != null) {
                                String urlStr = url.toString();
                                return exclude(urlStr);
                            }
                        }
                        return false;
                    })
                    .toArray(Resource[]::new);
        }
        return resources;
    }

    private static String apache = "apache";
    private static String tk = "tk.mybatis";

    private static boolean exclude(String urlStr) {
        if (urlStr.contains(apache)) {
            return false;
        }
        if (urlStr.contains(tk)) {
            return false;
        }
        return true;
    }
}
