package cn.gmlee.tools.log.config;

import cn.gmlee.tools.log.aop.ApiPrintAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({ApiPrintAspect.class})
public class ApiPrintAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApiPrintTrigger.class)
    public ApiPrintTrigger apiPrintTrigger(){
        return (log, result, e) -> {
            ApiPrintAutoConfiguration.log.info("临时日志...{}", log.site);
        };
    }

}
