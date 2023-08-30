package cn.gmlee.tools.gray.assist;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.gray.conf.GrayProperties;
import org.springframework.cloud.client.ServiceInstance;

import java.util.Map;

/**
 * 实例辅助类.
 */
public class InstanceAssist {

    /**
     * 获取版本号.
     *
     * @param instance   the instance
     * @param properties the properties
     * @return the boolean
     */
    public static String version(ServiceInstance instance, GrayProperties properties){
        Map<String, String> metadata = instance.getMetadata();
        if(BoolUtil.isEmpty(metadata)){
            return null;
        }
        return metadata.get(properties.getHead());
    }

    /**
     * 匹配版本号.
     *
     * @param instance   the instance
     * @param properties the properties
     * @return the boolean
     */
    public static boolean matching(ServiceInstance instance, GrayProperties properties) {
        if (BoolUtil.isEmpty(properties.getVersions())){
            // 没有指定灰度版本的; 只要有版本号都可以作为灰度 (ps: 以最新版本号的实例作为灰度)
            return true;
        }
        String version = version(instance, properties);
        return properties.getVersions().contains(version);
    }
}
