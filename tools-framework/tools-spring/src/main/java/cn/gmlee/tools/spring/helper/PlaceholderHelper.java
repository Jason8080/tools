package cn.gmlee.tools.spring.helper;

import cn.gmlee.tools.base.util.BoolUtil;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 占位符替换工具.
 */
public class PlaceholderHelper {

    private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");

    /**
     * 占位符替换工具.
     *
     * @param content the content
     * @param map     the map
     * @return the string
     */
    public static String replace(String content, Map map) {
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        Properties properties = new Properties();
        if(BoolUtil.notEmpty(map)){
            Set keys = map.keySet();
            for (Object key : keys){
                Object val = map.get(key);
                if (val == null){
                    map.put(key, "");
                }
            }
            properties.putAll(map);
        }
        return helper.replacePlaceholders(content, properties);
    }

    /**
     * Handle string.
     *
     * @param content the content
     * @param key     the key
     * @param val     the val
     * @return the string
     */
    public static String replace(String content, String key, Object val) {
        if (BoolUtil.isEmpty(content)) {
            return content;
        }
        Properties properties = new Properties();
        properties.put(key, val != null ? val.toString() : "");
        return helper.replacePlaceholders(content, properties);
    }
}
