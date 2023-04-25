package cn.gmlee.tools.webapp.config.cors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 注册中心的配置类
 *
 * @author Jas°
 * @date 2020 /11/30 (周一)
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tools.webapp.cors")
public class CorsProperties {
    /**
     * 拦截路径
     */
    private String path = "/**";
    /**
     * 允许域名
     */
    private List<String> origins = Arrays.asList("*");
    /**
     * 允许请求头
     */
    private List<String> headers = Arrays.asList("*");
    /**
     * 允许请求方法
     */
    private List<String> methods = Arrays.asList("*");
    /**
     * 允许响应头: 默认允许所有(也就是null值)
     */
    private List<String> exposedHeaders;
    /**
     * 允许有效时间: 有效时间内无需重复发起预检请求
     */
    private Long maxAge = 3600L;
    /**
     * 允许发送 cookie
     */
    private Boolean credentials = true;
}
