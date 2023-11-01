package cn.gmlee.tools.cache2.config;

import cn.gmlee.tools.cache2.server.cache.CacheServer;
import cn.gmlee.tools.cache2.server.cache.MemoryCacheServer;
import cn.gmlee.tools.cache2.server.cache.RedisCacheServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

public class CacheServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheServer.class)
    public CacheServer memoryCacheServer() {
        MemoryCacheServer memoryCacheServer = new MemoryCacheServer();
        memoryCacheServer.setMemory(new HashMap());
        return memoryCacheServer;
    }

    @Bean
    @ConditionalOnMissingBean(CacheServer.class)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    public CacheServer redisCacheServer() {
        return new RedisCacheServer();
    }

}