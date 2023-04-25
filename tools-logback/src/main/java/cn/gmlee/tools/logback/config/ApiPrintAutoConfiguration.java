package cn.gmlee.tools.logback.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiPrintAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApiPrintTrigger.class)
    public ApiPrintTrigger apiPrintTrigger(){
        return (jsonLog, result, e) -> {};
    }

}
