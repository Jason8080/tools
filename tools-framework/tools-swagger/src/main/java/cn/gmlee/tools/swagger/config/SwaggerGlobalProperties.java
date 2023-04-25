package cn.gmlee.tools.swagger.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Swagger全局配置
 *
 * @author Jas °
 */
@Data
@ConfigurationProperties(prefix = "tools.webapp.swagger.global")
public class SwaggerGlobalProperties {
    public Boolean enable = true;
    public String parametersIndex = "0,1";
    private List<GlobalParameter> parameters = new ArrayList();

    @Data
    public static class GlobalParameter implements Serializable {
        private String name;
        private String description;
        private String defaultValue;
        private Boolean required = false;
        private String paramType = "header";
        private String javaType = "string";
        private Boolean hidden = false;
    }
}
