package cn.gmlee.tools.gray.conf;

import cn.gmlee.tools.gray.server.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 灰度通用服务自动装配
 */
@EnableConfigurationProperties(GrayProperties.class)
public class GrayCommonAutoConfiguration {

    /**
     * Gray server gray server.
     *
     * @param properties the properties
     * @return the gray server
     */
    @Bean
    @ConditionalOnMissingBean({GrayServer.class})
    public GrayServer grayServer(GrayProperties properties) {
        return new GrayServer(properties) {
            @Override
            public String getUserId(String token) {
                log.warn("灰度令牌用户编号未实现: {}", token);
                return token;
            }

            @Override
            public String getUserName(String token) {
                log.warn("灰度令牌用户名称规则未实现: {}", token);
                return token;
            }

            @Override
            public Boolean extend(String app, String token) {
                log.warn("灰度令牌定制扩展规则未实现: {}", token);
                return false;
            }
        };
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