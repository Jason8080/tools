package cn.gmlee.tools.redis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.*;

/**
 * .
 *
 * @author Jas°
 * @date 2021/10/11 (周一)
 */
@EnableCaching
@Configuration
@SuppressWarnings("all")
@ConditionalOnBean(RedisTemplate.class)
public class OperationsConfiguration {
    /**
     * 对hash类型的数据操作
     *
     * @param redisTemplate the redis template
     * @return hash operations
     */
    @Bean
    public HashOperations<Object, Object, Object> hashOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * 对redis字符串类型数据操作
     *
     * @param redisTemplate the redis template
     * @return value operations
     */
    @Bean
    public ValueOperations<Object, Object> valueOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * 对链表类型的数据操作
     *
     * @param redisTemplate the redis template
     * @return list operations
     */
    @Bean
    public ListOperations<Object, Object> listOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * 对无序集合类型的数据操作
     *
     * @param redisTemplate the redis template
     * @return operations
     */
    @Bean
    public SetOperations<Object, Object> setOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * 对有序集合类型的数据操作
     *
     * @param redisTemplate the redis template
     * @return z set operations
     */
    @Bean
    public ZSetOperations<Object, Object> zSetOperations(RedisTemplate<Object, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}
