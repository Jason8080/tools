package cn.gmlee.tools.dt.conf;

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
@ConfigurationProperties(prefix = "tools.dt")
public class DtProperties {
    private Long appId = 0L;
    private String ip = "127.0.0.1";
    private Integer port = 9527;
    private Long timeout = 10L;
}
