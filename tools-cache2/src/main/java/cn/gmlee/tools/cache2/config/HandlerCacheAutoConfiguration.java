package cn.gmlee.tools.cache2.config;


import cn.gmlee.tools.cache2.handler.CacheHandler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

@AutoConfigureAfter({
        CacheServerAutoConfiguration.class,
        DsCacheServerAutoConfiguration.class,
        AdapterCacheAutoConfiguration.class,
})
public class HandlerCacheAutoConfiguration {

    @Bean
    public CacheHandler cacheHandler() {
//        cacheHandler.setCacheServer(cacheServer);
//        cacheHandler.setDsServers(dsServers);
//        cacheHandler.setFsAdapters(fsAdapters);
        return new CacheHandler();
    }
}
