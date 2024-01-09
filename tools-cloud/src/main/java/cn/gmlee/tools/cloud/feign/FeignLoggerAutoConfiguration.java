package cn.gmlee.tools.cloud.feign;

import feign.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@AutoConfigureAfter(FeignAutoConfiguration.class)
@EnableConfigurationProperties(FeignLoggerProperties.class)
public class FeignLoggerAutoConfiguration {

    @Autowired
    private FeignLoggerProperties feignLoggerProperties;


    @Bean
    public FeignLogger feignLogger(FeignClientProperties props) {
        Map<String, FeignClientProperties.FeignClientConfiguration> configurationMap = props.getConfig();
        FeignClientProperties.FeignClientConfiguration configuration = configurationMap.get(props.getDefaultConfig());
        if(configuration == null){
            configuration = new FeignClientProperties.FeignClientConfiguration();
            configurationMap.put(props.getDefaultConfig(), configuration);
        }
        configuration.setLoggerLevel(Logger.Level.FULL);
        return new FeignLogger(feignLoggerProperties);
    }
}