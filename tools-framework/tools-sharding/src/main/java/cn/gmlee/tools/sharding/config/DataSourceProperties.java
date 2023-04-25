package cn.gmlee.tools.sharding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * The type Sharding config properties.
 *
 * @author Jas °
 * @date 2021 /7/6 (周二)
 */
@Data
@ConfigurationProperties(prefix = "spring")
public class DataSourceProperties {
    /**
     * 数据源配置
     */
    private Datasource datasource = new Datasource();

    @Data
    public class Datasource {
        /**
         * 基础配置数据库
         */
        private Map<String, String> cof = Collections.emptyMap();
        /**
         * 默认业务数据库
         */
        private Map<String, String> def = Collections.emptyMap();
    }
}
