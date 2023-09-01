package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.server.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 灰度服务自动装配
 */
@EnableConfigurationProperties(GrayProperties.class)
public class GrayServerAutoConfiguration {

    /**
     * Gray server gray server.
     *
     * @param properties the properties
     * @return the gray server
     */
    @Bean
    @ConditionalOnMissingBean({GrayServer.class})
    public GrayServer grayServer(GrayProperties properties) {
        return new GrayServer(properties);
    }

    /**
     * Ip handler ip handler.
     *
     * @param grayServer the gray server
     * @return the ip handler
     */
    @Bean
    @ConditionalOnMissingBean({IpHandler.class})
    public IpHandler ipHandler(GrayServer grayServer) {
        return new IpHandler(grayServer);
    }

    /**
     * User handler user handler.
     *
     * @param grayServer the gray server
     * @return the user handler
     */
    @Bean
    @ConditionalOnMissingBean({UserHandler.class})
    public UserHandler userHandler(GrayServer grayServer) {
        return new UserHandler(grayServer);
    }

    /**
     * Weight handler weight handler.
     *
     * @param grayServer the gray server
     * @return the weight handler
     */
    @Bean
    @ConditionalOnMissingBean({WeightHandler.class})
    public WeightHandler weightHandler(GrayServer grayServer) {
        return new WeightHandler(grayServer);
    }

    /**
     * Custom handler custom handler.
     *
     * @param grayServer the gray server
     * @return the custom handler
     */
    @Bean
    @ConditionalOnMissingBean({CustomHandler.class})
    public CustomHandler customHandler(GrayServer grayServer) {
        return new CustomHandler(grayServer);
    }
}