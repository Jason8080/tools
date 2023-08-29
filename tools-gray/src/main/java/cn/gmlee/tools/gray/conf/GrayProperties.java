package cn.gmlee.tools.gray.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Gray参数
 */
@Data
@ConfigurationProperties(prefix = "tools.gray")
public class GrayProperties {
    private String evn = "env";
    private String head = "TOOLS-GRAY";
    private String version = "1.0.0";
    /**
     * 节点灰度的开关.
     */
    private String enable = "true";
    /**
     * 排除的接口地址.
     */
    private List<String> excludes = new ArrayList<>();
}
