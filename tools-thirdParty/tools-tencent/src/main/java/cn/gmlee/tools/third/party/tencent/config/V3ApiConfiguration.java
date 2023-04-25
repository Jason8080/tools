package cn.gmlee.tools.third.party.tencent.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * The type V 3 api configuration.
 *
 * @author Jas °
 */
@Data
@Configuration
@PropertySource(value = {"classpath:v3-api.properties"}, ignoreResourceNotFound = true)
public class V3ApiConfiguration {
    @Value("${tools.tencent.v3api.key:#{null}}")
    private String key;
}
