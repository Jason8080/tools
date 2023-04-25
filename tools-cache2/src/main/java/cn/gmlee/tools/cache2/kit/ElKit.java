package cn.gmlee.tools.cache2.kit;

import cn.gmlee.tools.base.util.BoolUtil;
import cn.gmlee.tools.base.util.CollectionUtil;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Map;
import java.util.Properties;

/**
 * The type El kit.
 */
public class ElKit {
    private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");

    /**
     * Parse string.
     *
     * @param content the content
     * @param map     the map
     * @return the string
     */
    public static String parse(String content, Map map) {
        if(BoolUtil.isEmpty(content) || BoolUtil.isEmpty(map)) {
            return content;
        }
        CollectionUtil.filter(map, (key, value) -> value!=null);
        Properties properties = new Properties();
        properties.putAll(map);
        return helper.replacePlaceholders(content, properties);
    }
}
