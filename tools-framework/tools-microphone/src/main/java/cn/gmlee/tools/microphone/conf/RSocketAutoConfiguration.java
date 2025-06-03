package cn.gmlee.tools.microphone.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * RSocket自动装配.
 */
@Configuration(proxyBeanMethods = false)
@PropertySource(value = {"classpath:microphone.properties", "classpath:microphone.properties"}, ignoreResourceNotFound = true)
public class RSocketAutoConfiguration {
}