package cn.gmlee.tools.redis.assist;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * Redis序列化辅助工具
 *
 * @author Jas°
 * @date 2021 /2/3 (周三)
 */
public class SerializerAssist {
    /**
     * 获取Jackson基础配置.
     *
     * @param activateDefaultTyping
     * @return the object mapper
     */
    public static ObjectMapper getObjectMapper(Boolean activateDefaultTyping) {
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // false: 关闭泛型支持, 存储到纯json内容,取出是LikeHashMap (不好用)
        if (activateDefaultTyping) {
            // 激活类型信息: 存储到redis的是带类路径的内容,可以直接反序列化成对象 (方便使用)
            om.activateDefaultTyping(
                    LaissezFaireSubTypeValidator.instance,
                    ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY
            );
        }
        return om;
    }
}
