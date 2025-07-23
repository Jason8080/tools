package cn.gmlee.tools.cloud.feign;

import cn.gmlee.tools.cloud.fallback.ApiFallbackFactory;
import org.springframework.context.annotation.Bean;

/**
 * The type Fallback factory auto configuration.
 */
public class FallbackFactoryAutoConfiguration {
    /**
     * Generic fallback factory generic fallback factory.
     *
     * @return the generic fallback factory
     */
    @Bean
    public ApiFallbackFactory genericFallbackFactory() {
        return new ApiFallbackFactory();
    }
}