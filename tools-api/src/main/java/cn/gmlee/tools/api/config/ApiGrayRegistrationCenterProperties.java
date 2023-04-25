package cn.gmlee.tools.api.config;

import cn.gmlee.tools.api.gray.model.Gray;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "tools.api")
public class ApiGrayRegistrationCenterProperties {
    /**
     * urlPatterns : Gray
     * urlPatterns 默认 /*
     */
    List<Gray> grays = new ArrayList();
}
