package cn.gmlee.tools.ds.config.dynamic;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用数据源属性配置
 *
 * @author Jas°
 * @date 2020/11/25 (周三)
 */
@Data
@ConfigurationProperties(prefix = "spring")
@PropertySource(value = {"classpath:mysql.properties","classpath:application.properties"}, ignoreResourceNotFound = true)
public class DynamicDataSourceProperties {
    Map<String, Map<String, String>> datasource = new LinkedHashMap();
    Map<String, String> druid = new HashMap();
}
