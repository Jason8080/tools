package cn.gmlee.tools.cache2.config;

import cn.gmlee.tools.cache2.aspect.CacheAspect;
import cn.gmlee.tools.cache2.server.cache.CacheServer;
import cn.gmlee.tools.cache2.server.cache.MemoryCacheServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

@ConditionalOnBean(CacheAspect.class)
public class MemoryCacheServerConfig {
    @Bean
    @ConditionalOnMissingBean(CacheServer.class)
    public MemoryCacheServer memoryCacheServer() {
        MemoryCacheServer memoryCacheServer = new MemoryCacheServer();
        memoryCacheServer.setMemory(new HashMap());
        return memoryCacheServer;
    }
}