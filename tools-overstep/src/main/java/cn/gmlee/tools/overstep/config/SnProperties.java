package cn.gmlee.tools.overstep.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 注册中心的配置类
 *
 * @author Jas°
 * @date 2020 /11/30 (周一)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tools.overstep")
public class SnProperties {
    /**
     * 允许明文传输
     */
    private Boolean allowPlaintext = false;
    /**
     * 密钥: 请妥善保管
     */
    private String secretKey = "TOOLS";
    /**
     * 加解密字段
     */
    private List<String> fields = new ArrayList();
    /**
     * 非排除模式既选用模式
     */
    private Boolean exclude = true;
    /**
     * 路径规则.
     * <p>
     *     注意1: exclude为true表示该路径需要排除越权方案
     *     注意2: exclude为false表示该路径需要使用越权方案
     * </p>
     */
    private List<String> urls = new ArrayList();
}
