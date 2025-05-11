package cn.gmlee.tools.oss.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("tools.oss.sts")
public class StsProperties {
    private String roleArn = "acs:ram::1136911228867966:role/oss";
    private String roleSessionName = "TOOLS-OSS-STS";
    private Long duration = 900L; // 阿里: 最大15分钟
    private Integer redundancy = 60 * 1000;;
    private Long expire = System.currentTimeMillis();
}
