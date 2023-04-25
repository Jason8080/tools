package cn.gmlee.tools.cache2.config;

import cn.gmlee.tools.cache2.aspect.CacheAspect;
import cn.gmlee.tools.cache2.server.cache.CacheServer;
import cn.gmlee.tools.cache2.server.cache.RedisCacheServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@ConditionalOnBean(CacheAspect.class)
public class RedisCacheServerConfig {

    @Value("${tools.cache2.key:TOOLS:CACHE2:KEY_}")
    private String cacheKey;

    @Bean
    @ConditionalOnMissingBean(CacheServer.class)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    public RedisCacheServer redisCacheServer(RedisTemplate redisTemplate) {
        RedisCacheServer redisCacheServer = new RedisCacheServer();
        redisCacheServer.setCacheKey(cacheKey);
        redisCacheServer.setRedis(redisTemplate);
        return redisCacheServer;
    }
}