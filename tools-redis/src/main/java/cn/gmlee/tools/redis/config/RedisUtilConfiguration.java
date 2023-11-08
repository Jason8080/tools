package cn.gmlee.tools.redis.config;

import cn.gmlee.tools.redis.lock.VariableLockServer;
import cn.gmlee.tools.redis.util.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Redis util configuration.
 *
 * @author Jas °
 * @date 2020 /10/30 (周五)
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnBean({RedisClient.class, RedisLock.class, RedisId.class})
public class RedisUtilConfiguration {
    /**
     * Redis util redis util.
     *
     * @param redisClient the redis client
     * @param redisLock   the redis lock
     * @param redisId     the redis id
     * @return the redis util
     */
    @Bean
    public RedisUtil redisUtil(RedisClient redisClient, RedisLock redisLock, RedisId redisId) {
        return new RedisUtil(redisClient, redisLock, redisId);
    }

    /**
     * Variable lock util variable lock util.
     *
     * @param variableLockServer the variable lock server
     * @return the variable lock util
     */
    @Bean
    public VariableLockUtil variableLockUtil(VariableLockServer variableLockServer) {
        return new VariableLockUtil(variableLockServer);
    }
}
