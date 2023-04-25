package cn.gmlee.tools.sharding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * The type Sharding config properties.
 *
 * @author Jas °
 * @date 2021 /7/6 (周二)
 */
@Data
@ConfigurationProperties(prefix = "tools.sharding")
public class ShardingConfigProperties {
    /**
     * 基础配置
     */
    private Long sysId = 0L;
    private Boolean enable = true;
    /**
     * 定时配置
     */
    private Cron cron = new Cron();
    /**
     * 容量配置
     */
    private Volume volume = new Volume();
    /**
     * 原生配置
     */
    private Properties props = new Properties();

    /**
     * The type Cron.
     */
    @Data
    public class Cron {
        private String cutDataSource = "0 0 0 * * ?";
        private String observeDataSize = "* * 23 * * ?";
    }

    /**
     * The type Volume.
     */
    @Data
    public class Volume {
        private Integer tableUpper = 10;
        private Integer rowUpper = 300 * 10000;
    }
}
