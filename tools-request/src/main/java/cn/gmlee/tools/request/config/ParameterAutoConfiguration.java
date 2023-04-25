package cn.gmlee.tools.request.config;

import cn.gmlee.tools.request.aop.DesensitizationAspect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * 参数拦截器.
 * <p>
 *     参数拦截器包含: 脱敏和加密功能.
 * </p>
 *
 * @author Jas °
 */
@EnableConfigurationProperties({ParameterProperties.class, EncryptProperties.class, DesensitizationProperties.class})
public class ParameterAutoConfiguration {

    @Resource
    private ParameterProperties parameterProperties;

    @Resource
    private EncryptProperties encryptProperties;

    @Resource
    private DesensitizationProperties desensitizationProperties;

    @Bean
    public DesensitizationAspect desensitizationAspect(){
        return new DesensitizationAspect(parameterProperties, encryptProperties, desensitizationProperties);
    }
}
