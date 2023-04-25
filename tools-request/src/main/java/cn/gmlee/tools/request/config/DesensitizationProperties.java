package cn.gmlee.tools.request.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通用数据源属性配置
 *
 * @author Jas°
 * @date 2020/11/25 (周三)
 */
@Data
@ConfigurationProperties(prefix = "tools.request.desensitization")
public class DesensitizationProperties {
    private Boolean enable = true;
}
