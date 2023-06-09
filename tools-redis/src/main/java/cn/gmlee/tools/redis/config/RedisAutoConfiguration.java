package cn.gmlee.tools.redis.config;

import cn.gmlee.tools.base.util.ExceptionUtil;
import cn.gmlee.tools.redis.assist.RedisAssist;
import cn.gmlee.tools.redis.assist.SerializerAssist;
import cn.gmlee.tools.redis.util.RedisClient;
import cn.gmlee.tools.redis.util.RedisId;
import cn.gmlee.tools.redis.util.RedisLock;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

/**
 * redis 配置类
 *
 * @author Jas°
 * @date 2020 /8/28 (周五)
 */
@Order
@AutoConfigureBefore(org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@PropertySource(value = {"classpath:redis.properties", "classpath:application.properties"}, ignoreResourceNotFound = true)
public class RedisAutoConfiguration extends CachingConfigurerSupport {

    /**
     * 是否激活泛型支持.
     * <p>
     * false: 关闭泛型支持, 存储到纯json内容, 不作处理时取出是LikeHashMap (原生)
     * true: 激活类型信息: 存储到redis的是带类路径的内容,直接反序列化成对象 (方便使用,前提是路径字节码存在)
     * </p>
     */
    @Value("${tools.redis.serializer.activateDefaultTyping:false}")
    private Boolean activateDefaultTyping;

    /**
     * 是否写入原生的字符串.
     * <p>
     * false: 将采用jackson序列化; 字符串将被转义(" -> \")
     * true
     * </p>
     */
    @Value("${tools.redis.serializer.valueStrategy:json}")
    private String valueStrategy;


    @Bean
    @ConditionalOnMissingBean(RedisSerializer.class)
    @ConditionalOnProperty("tools.redis.serializer.valueStrategy")
    public RedisSerializer redisSerializer() {
        ObjectMapper objectMapper = SerializerAssist.getObjectMapper(activateDefaultTyping);
        switch (valueStrategy){
            case "json" : return new GenericJackson2JsonRedisSerializer(objectMapper);
            case "string" : return new GenericToStringSerializer(Object.class);
            case "jdk" : return new JdkSerializationRedisSerializer();
            case "oxm" : return new OxmSerializer();
        }
        return ExceptionUtil.cast(String.format("暂不支持%s序列化", valueStrategy));
    }


    @Bean
    @ConditionalOnMissingBean(RedisClient.class)
    public RedisClient redisClient(RedisConnectionFactory redisConnectionFactory, RedisSerializer redisSerializer) {
        RedisAssist.setValidateConnection(redisConnectionFactory);
        return new RedisClient(RedisAssist.redisTemplateByJackson(redisConnectionFactory, redisSerializer));
    }

    @Bean
    @ConditionalOnMissingBean(RedisLock.class)
    public RedisLock redisLock(RedisConnectionFactory redisConnectionFactory) {
        RedisAssist.setValidateConnection(redisConnectionFactory);
        return new RedisLock(new RedisClient(RedisAssist.redisTemplateByBytes(redisConnectionFactory)));
    }

    @Bean
    @ConditionalOnMissingBean(RedisId.class)
    public RedisId redisId(RedisConnectionFactory redisConnectionFactory) {
        RedisAssist.setValidateConnection(redisConnectionFactory);
        return new RedisId(RedisAssist.redisTemplateByBytes(redisConnectionFactory));
    }
}
