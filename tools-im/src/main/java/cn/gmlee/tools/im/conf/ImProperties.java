package cn.gmlee.tools.im.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "im")
public class ImProperties {
    /**
     * 控制器根路径，默认 /
     */
    private String basePath = "/";
}
