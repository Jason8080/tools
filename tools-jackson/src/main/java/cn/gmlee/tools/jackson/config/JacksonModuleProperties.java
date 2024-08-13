package cn.gmlee.tools.jackson.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Module 注册.
 */
@Data
@ConfigurationProperties(prefix = "tools.jackson.module")
public class JacksonModuleProperties {
    /**
     * 类型转换模块.
     */
    private Boolean longToString = true;
}
