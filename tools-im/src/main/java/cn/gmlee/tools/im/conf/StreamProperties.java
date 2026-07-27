package cn.gmlee.tools.im.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "im.stream")
public class StreamProperties {
}
