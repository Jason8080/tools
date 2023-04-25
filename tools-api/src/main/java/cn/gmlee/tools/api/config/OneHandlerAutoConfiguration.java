package cn.gmlee.tools.api.config;

import cn.gmlee.tools.api.once.OnceHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Once处理器自动注入.
 *
 * @author Jas°
 * @date 2020/8/28 (周五)
 */
@ConditionalOnMissingBean(OnceHandler.class)
public class OneHandlerAutoConfiguration {
    @Bean
    public OnceHandler onceHandler(){
        return new OnceHandler() {};
    }
}
