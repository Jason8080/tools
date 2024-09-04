package cn.gmlee.tools.log.config;

import cn.gmlee.tools.log.http.OkHttpInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class HttpLoggerAutoConfiguration {

    @Bean
    @ConditionalOnClass(OkHttpInterceptor.class)
    @ConditionalOnMissingBean(OkHttpInterceptor.class)
    public OkHttpInterceptor okHttpInterceptor(){
        return new OkHttpInterceptor();
    }
}
