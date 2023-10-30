package cn.gmlee.tools.cache2.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;


@ImportAutoConfiguration({
        AdapterCacheAutoConfiguration.class,
        DsCacheServerAutoConfiguration.class,
        CacheServerAutoConfiguration.class,
        HandlerCacheAutoConfiguration.class,
        AspectCacheAutoConfiguration.class,
})
public class Cache2AutoConfiguration {

}
