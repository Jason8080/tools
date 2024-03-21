package cn.gmlee.tools.cloud.feign;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "tools.cloud")
public class FeignLoggerProperties {

    private FeignLog feignLog = new FeignLog();

    @Data
    public static class FeignLog {
        public Integer maxlength = 4096;
        public List<String> excludeUrls = Collections.emptyList();
    }
}
