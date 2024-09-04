package cn.gmlee.tools.log.config;

import cn.gmlee.tools.log.aop.ApiPrintAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ApiPrintAspect.class})
public class ApiPrintAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApiPrintTrigger.class)
    public ApiPrintTrigger apiPrintTrigger(){
        return (jsonLog, result, e) -> {};
    }

}
