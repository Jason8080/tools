package cn.gmlee.tools.redis.config;


import cn.gmlee.tools.redis.aop.VariableLockAspect;
import cn.gmlee.tools.redis.lock.MemoryVariableLockServer;
import cn.gmlee.tools.redis.lock.RedisVariableLockServer;
import cn.gmlee.tools.redis.lock.VariableLockServer;
import cn.gmlee.tools.redis.util.RedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 变量锁自动装载.
 */
@Import({
        VariableLockAspect.class,
        VariableLockAutoConfiguration.RedisServerAutoConfiguration.class,
        VariableLockAutoConfiguration.MemoryServerAutoConfiguration.class,
})
public class VariableLockAutoConfiguration {

    @ConditionalOnClass(RedisClient.class)
    static class RedisServerAutoConfiguration{
        @Bean
        @ConditionalOnMissingBean(VariableLockServer.class)
        public VariableLockServer variableLockServer(){
            return new RedisVariableLockServer();
        }
    }

    @ConditionalOnMissingClass("cn.gmlee.tools.redis.util.RedisClient")
    static class MemoryServerAutoConfiguration{
        @Bean
        @ConditionalOnMissingBean(VariableLockServer.class)
        public VariableLockServer variableLockServer(){
            return new MemoryVariableLockServer();
        }
    }
}
