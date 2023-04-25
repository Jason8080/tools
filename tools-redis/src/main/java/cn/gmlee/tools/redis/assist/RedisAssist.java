package cn.gmlee.tools.redis.assist;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 通用Redis连接工厂配置
 *
 * @author Jas°
 * @date 2021 /2/3 (周三)
 */
public class RedisAssist {

    /**
     * 使用Jackson序列化方案持久化value值: [用于存储/不支持自增].
     *
     * @param factory the factory
     * @return redis template
     */
    public static RedisTemplate redisTemplateByJackson(RedisConnectionFactory factory, RedisSerializer redisSerializer) {
        // 是否允许多个线程操作共用同一个缓存连接，factory.setShareNativeConnection(true)
        // 默认true，false时每个操作都将开辟新的连接
        RedisTemplate template = new RedisTemplate();
        // 配置连接工厂
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisSerializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisSerializer);
        template.afterPropertiesSet();

        return template;
    }


    /**
     * 使用通用序列化方案持久化value值: [支持自增].
     *
     * @param factory the factory
     * @return the redis template
     */
    public static RedisTemplate redisTemplateByBytes(RedisConnectionFactory factory) {
        RedisTemplate template = new RedisTemplate();
        // 配置连接工厂
        template.setConnectionFactory(factory);
        GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Object.class);

        template.setValueSerializer(genericToStringSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(genericToStringSerializer);

        template.afterPropertiesSet();
        return template;
    }


    public static void setValidateConnection(RedisConnectionFactory factory) {
        if (factory instanceof LettuceConnectionFactory) {
            // 使用前先校验连接解决Connection reset by peer问题
            ((LettuceConnectionFactory) factory).setValidateConnection(true);
        }
    }
}
