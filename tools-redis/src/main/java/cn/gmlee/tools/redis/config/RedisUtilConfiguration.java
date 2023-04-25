package cn.gmlee.tools.redis.config;

import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.redis.util.RedisId;
import cn.gmlee.tools.redis.util.RedisLock;
import cn.gmlee.tools.redis.util.RedisUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jas°
 * @date 2020/10/30 (周五)
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnBean({RedisClient.class, RedisLock.class, RedisId.class})
public class RedisUtilConfiguration {
    @Bean
    public RedisUtil redisUtil(RedisClient redisClient, RedisLock redisLock, RedisId redisId) {
        return new RedisUtil(redisClient, redisLock, redisId);
    }
}
